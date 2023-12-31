package codesquad.fineants.spring.api.watch_list;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import codesquad.fineants.domain.member.Member;
import codesquad.fineants.domain.member.MemberRepository;
import codesquad.fineants.domain.oauth.support.AuthMember;
import codesquad.fineants.domain.stock.Stock;
import codesquad.fineants.domain.stock.StockRepository;
import codesquad.fineants.domain.watch_list.WatchList;
import codesquad.fineants.domain.watch_list.WatchListRepository;
import codesquad.fineants.domain.watch_stock.WatchStock;
import codesquad.fineants.domain.watch_stock.WatchStockRepository;
import codesquad.fineants.spring.api.errors.errorcode.MemberErrorCode;
import codesquad.fineants.spring.api.errors.errorcode.StockErrorCode;
import codesquad.fineants.spring.api.errors.errorcode.WatchListErrorCode;
import codesquad.fineants.spring.api.errors.exception.ForBiddenException;
import codesquad.fineants.spring.api.errors.exception.NotFoundResourceException;
import codesquad.fineants.spring.api.kis.manager.CurrentPriceManager;
import codesquad.fineants.spring.api.kis.manager.LastDayClosingPriceManager;
import codesquad.fineants.spring.api.watch_list.request.CreateWatchListRequest;
import codesquad.fineants.spring.api.watch_list.request.CreateWatchStockRequest;
import codesquad.fineants.spring.api.watch_list.response.CreateWatchListResponse;
import codesquad.fineants.spring.api.watch_list.response.ReadWatchListResponse;
import codesquad.fineants.spring.api.watch_list.response.ReadWatchListsResponse;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class WatchListService {

	private final WatchListRepository watchListRepository;
	private final MemberRepository memberRepository;
	private final StockRepository stockRepository;
	private final WatchStockRepository watchStockRepository;
	private final CurrentPriceManager currentPriceManager;
	private final LastDayClosingPriceManager lastDayClosingPriceManager;

	@Transactional
	public CreateWatchListResponse createWatchList(AuthMember authMember, CreateWatchListRequest request) {
		Member member = findMember(authMember.getMemberId());
		WatchList watchList = WatchList.builder()
			.member(member)
			.name(request.getName())
			.build();
		watchList = watchListRepository.save(watchList);
		return new CreateWatchListResponse(watchList.getId());
	}

	@Transactional(readOnly = true)
	public List<ReadWatchListsResponse> readWatchLists(AuthMember authMember) {
		Member member = findMember(authMember.getMemberId());
		List<WatchList> watchLists = watchListRepository.findByMember(member);
		return ReadWatchListsResponse.from(watchLists);
	}

	@Transactional(readOnly = true)
	public List<ReadWatchListResponse> readWatchList(AuthMember authMember, Long watchListId) {
		Member member = findMember(authMember.getMemberId());
		WatchList watchList = watchListRepository.findById(watchListId)
			.orElseThrow(() -> new NotFoundResourceException(WatchListErrorCode.NOT_FOUND_WATCH_LIST));

		validateWatchListAuthorization(member.getId(), watchList.getMember().getId());

		List<WatchStock> watchStocks = watchStockRepository.findWithStockAndDividendsByWatchList(watchList);

		return watchStocks.stream()
			.map(watchStock -> ReadWatchListResponse.from(watchStock, currentPriceManager, lastDayClosingPriceManager))
			.collect(Collectors.toList());
	}

	@Transactional
	public void deleteWatchList(AuthMember authMember, Long watchListId) {
		Member member = findMember(authMember.getMemberId());

		Optional<WatchList> watchList = watchListRepository.findById(watchListId);
		if(watchList.isPresent()){
			validateWatchListAuthorization(member.getId(), watchList.get().getMember().getId());
			watchListRepository.deleteById(watchListId);
		}
	}

	@Transactional
	public void createWatchStock(AuthMember authMember, Long watchListId, CreateWatchStockRequest request) {
		Member member = findMember(authMember.getMemberId());
		WatchList watchList = watchListRepository.findById(watchListId)
			.orElseThrow(() -> new NotFoundResourceException(WatchListErrorCode.NOT_FOUND_WATCH_LIST));

		validateWatchListAuthorization(member.getId(), watchList.getMember().getId());

		Stock stock = stockRepository.findByTickerSymbol(request.getTickerSymbol())
			.orElseThrow(() -> new NotFoundResourceException(StockErrorCode.NOT_FOUND_STOCK));

		WatchStock watchStock = WatchStock.builder()
			.watchList(watchList)
			.stock(stock)
			.build();
		watchStockRepository.save(watchStock);
	}

	@Transactional
	public void deleteWatchStock(AuthMember authMember, Long watchListId, Long stockId) {
		Member member = findMember(authMember.getMemberId());
		validateWatchListAuthorization(member.getId(), watchListId);

		WatchStock watchStock = watchStockRepository.findById(stockId)
			.orElseThrow(() -> new NotFoundResourceException(WatchListErrorCode.NOT_FOUND_WATCH_STOCK));

		if(!watchStock.getWatchList().getId().equals(watchListId)){
			throw new NotFoundResourceException(WatchListErrorCode.NOT_FOUND_WATCH_STOCK);
		}

		watchStockRepository.deleteById(stockId);
	}

	private Member findMember(Long memberId) {
		return memberRepository.findById(memberId)
			.orElseThrow(() -> new NotFoundResourceException(MemberErrorCode.NOT_FOUND_MEMBER));
	}

	private void validateWatchListAuthorization(Long memberId, Long watchListMemberId){
		if (!memberId.equals(watchListMemberId)) {
			throw new ForBiddenException(WatchListErrorCode.FORBIDDEN);
		}
	}
}
