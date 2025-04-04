package co.fineants.api.domain.watchlist.service;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import co.fineants.AbstractContainerBaseTest;
import co.fineants.api.domain.common.money.Money;
import co.fineants.api.domain.common.money.Percentage;
import co.fineants.api.domain.dividend.repository.StockDividendRepository;
import co.fineants.api.domain.kis.client.KisCurrentPrice;
import co.fineants.api.domain.kis.domain.dto.response.KisClosingPrice;
import co.fineants.api.domain.kis.repository.ClosingPriceRepository;
import co.fineants.api.domain.kis.repository.CurrentPriceRedisRepository;
import co.fineants.api.domain.member.domain.entity.Member;
import co.fineants.api.domain.member.repository.MemberRepository;
import co.fineants.api.domain.stock.domain.entity.Stock;
import co.fineants.api.domain.stock.repository.StockRepository;
import co.fineants.api.domain.watchlist.domain.dto.request.ChangeWatchListNameRequest;
import co.fineants.api.domain.watchlist.domain.dto.request.CreateWatchListRequest;
import co.fineants.api.domain.watchlist.domain.dto.request.CreateWatchStockRequest;
import co.fineants.api.domain.watchlist.domain.dto.request.DeleteWatchStocksRequest;
import co.fineants.api.domain.watchlist.domain.dto.response.CreateWatchListResponse;
import co.fineants.api.domain.watchlist.domain.dto.response.ReadWatchListResponse;
import co.fineants.api.domain.watchlist.domain.dto.response.ReadWatchListsResponse;
import co.fineants.api.domain.watchlist.domain.dto.response.WatchListHasStockResponse;
import co.fineants.api.domain.watchlist.domain.entity.WatchList;
import co.fineants.api.domain.watchlist.domain.entity.WatchStock;
import co.fineants.api.domain.watchlist.repository.WatchListRepository;
import co.fineants.api.domain.watchlist.repository.WatchStockRepository;
import co.fineants.api.global.errors.exception.temp.ForbiddenException;
import co.fineants.api.global.errors.exception.temp.WatchStockDuplicateException;

class WatchListServiceTest extends AbstractContainerBaseTest {

	@Autowired
	private WatchListService watchListService;

	@Autowired
	private MemberRepository memberRepository;

	@Autowired
	private WatchListRepository watchListRepository;

	@Autowired
	private CurrentPriceRedisRepository currentPriceRedisRepository;

	@Autowired
	private ClosingPriceRepository closingPriceRepository;

	@Autowired
	private WatchStockRepository watchStockRepository;

	@Autowired
	private StockRepository stockRepository;

	@Autowired
	private StockDividendRepository stockDividendRepository;

	@DisplayName("회원이 watchlist를 추가한다.")
	@Test
	void createWatchList() {
		// given
		Member member = memberRepository.save(createMember());
		CreateWatchListRequest request = new CreateWatchListRequest("My WatchList");

		// when
		CreateWatchListResponse response = watchListService.createWatchList(member.getId(), request);

		// then
		assertThat(response.getWatchlistId()).isNotNull();
	}

	@DisplayName("회원이 watchlist 목록을 조회한다.")
	@Test
	void readWatchLists() {
		//given
		Member member = memberRepository.save(createMember());
		watchListRepository.save(createWatchList("My WatchList 1", member));
		watchListRepository.save(createWatchList("My WatchList 2", member));

		//when
		List<ReadWatchListsResponse> response = watchListService.readWatchLists(member.getId());

		//then
		assertThat(response).hasSize(2);
	}

	@DisplayName("회원이 watchlist 단일 목록을 조회한다.")
	@Test
	void readWatchList() {
		// given
		Member member = memberRepository.save(createMember());
		Stock stock = stockRepository.save(createSamsungStock());
		stockDividendRepository.save(createStockDividend(LocalDate.now(), LocalDate.now(), stock));

		WatchList watchList = watchListRepository.save(createWatchList("My WatchList 1", member));
		watchStockRepository.save(createWatchStock(watchList, stock));

		currentPriceRedisRepository.savePrice(KisCurrentPrice.create("005930", 77000L));
		closingPriceRepository.addPrice(KisClosingPrice.create("005930", 77000L));

		setAuthentication(member);
		// when
		ReadWatchListResponse response = watchListService.readWatchList(member.getId(), watchList.getId());

		// then
		assertThat(response.getName()).isEqualTo(watchList.getName());
		assertThat(response)
			.extracting(ReadWatchListResponse::getWatchStocks)
			.asList()
			.extracting("companyName", "tickerSymbol", "currentPrice", "dailyChange", "dailyChangeRate",
				"annualDividendYield", "sector")
			.usingComparatorForType(Money::compareTo, Money.class)
			.usingComparatorForType(Percentage::compareTo, Percentage.class)
			.containsExactlyInAnyOrder(
				tuple(
					"삼성전자보통주",
					"005930",
					Money.won(77000),
					Money.zero(),
					Percentage.zero(),
					Percentage.from(0.0047),
					"전기전자"
				)
			);
	}

	@DisplayName("사용자는 다른 사용자의 watchlist 단일 목록을 조회할 수 없다")
	@Test
	void readWatchList_whenOtherMemberRead_thenThrowError() {
		// given
		Member member = memberRepository.save(createMember());
		Member hacker = memberRepository.save(createMember("hacker"));
		Stock stock = stockRepository.save(createSamsungStock());
		stockDividendRepository.save(createStockDividend(LocalDate.now(), LocalDate.now(), stock));

		WatchList watchList = watchListRepository.save(createWatchList("My WatchList 1", member));
		watchStockRepository.save(createWatchStock(watchList, stock));

		setAuthentication(hacker);
		// when
		Throwable throwable = catchThrowable(() -> watchListService.readWatchList(hacker.getId(), watchList.getId()));
		// then
		assertThat(throwable)
			.isInstanceOf(ForbiddenException.class)
			.hasMessage(watchList.toString());
	}

	@DisplayName("회원이 watchlist에 종목을 추가한다.")
	@Test
	void createWatchStocks() {
		// given
		String tickerSymbol = "005930";
		stockRepository.save(createSamsungStock());

		Member member = memberRepository.save(createMember());
		CreateWatchStockRequest request = new CreateWatchStockRequest(List.of(tickerSymbol));

		WatchList watchList = watchListRepository.save(createWatchList(member));
		Long watchListId = watchList.getId();

		setAuthentication(member);
		// when
		watchListService.createWatchStocks(member.getId(), watchListId, request);

		// then
		assertThat(watchStockRepository.findByWatchList(watchList)).hasSize(1);
		assertThat(watchStockRepository.findByWatchList(watchList).get(0).getStock().getTickerSymbol()).isEqualTo(
			tickerSymbol);
	}

	@DisplayName("사용자는 다른 사용자의 watchList에 종목을 추가할 수 없습니다")
	@Test
	void createWatchStocks_whenOtherMemberCreate_thenThrowException() {
		// given
		String tickerSymbol = "005930";
		stockRepository.save(createSamsungStock());

		Member member = memberRepository.save(createMember());
		Member hacker = memberRepository.save(createMember("hacker"));
		CreateWatchStockRequest request = new CreateWatchStockRequest(List.of(tickerSymbol));

		WatchList watchList = watchListRepository.save(createWatchList(member));
		Long watchListId = watchList.getId();

		setAuthentication(hacker);
		// when
		Throwable throwable = catchThrowable(
			() -> watchListService.createWatchStocks(hacker.getId(), watchListId, request));
		// then
		assertThat(throwable)
			.isInstanceOf(ForbiddenException.class)
			.hasMessage(watchList.toString());
	}

	@DisplayName("관심 종목 중복 추가 예외 케이스 시나리오")
	@TestFactory
	@Transactional
	Collection<DynamicTest> createWatchStocks_whenAlreadyStock_thenNotSaveStock() {
		return List.of(
			DynamicTest.dynamicTest("사용자는 관심 종목을 추가한다", () -> {
				// given
				Member member = memberRepository.save(createMember());
				stockRepository.save(createSamsungStock());

				CreateWatchListResponse watchlist1 = watchListService.createWatchList(member.getId(),
					new CreateWatchListRequest("watchlist1"));
				CreateWatchStockRequest request = new CreateWatchStockRequest(List.of("005930"));
				setAuthentication(member);
				// when
				watchListService.createWatchStocks(member.getId(), watchlist1.getWatchlistId(), request);

				// then
				assertThat(watchStockRepository.findAllById(List.of(watchlist1.getWatchlistId()))).hasSize(1);
			}),
			DynamicTest.dynamicTest("사용자는 관심 종목이 이미 존재하여 추가할 수 없다", () -> {
				// given
				Member member = memberRepository.findMemberByEmailAndProvider("dragonbead95@naver.com", "local")
					.orElseThrow();

				Long watchListId = watchListRepository.findByMember(member).stream()
					.findAny()
					.orElseThrow()
					.getId();
				CreateWatchStockRequest request = new CreateWatchStockRequest(List.of("005930"));

				setAuthentication(member);
				// when
				Throwable throwable = catchThrowable(
					() -> watchListService.createWatchStocks(member.getId(), watchListId, request));

				// then
				assertThat(throwable)
					.isInstanceOf(WatchStockDuplicateException.class)
					.hasMessage(List.of("005930").toString());
			})
		);
	}

	@DisplayName("회원이 watchlist를 삭제한다.")
	@Test
	void deleteWatchLists() {
		// given
		Stock stock = stockRepository.save(createSamsungStock());

		Member member = memberRepository.save(createMember());

		WatchList watchList = watchListRepository.save(createWatchList(member));
		Long watchListId = watchList.getId();

		watchStockRepository.save(createWatchStock(watchList, stock));

		setAuthentication(member);
		// when
		watchListService.deleteWatchLists(member.getId(), List.of(watchListId));

		// then
		assertThat(watchListRepository.findById(watchListId)).isEmpty();
		assertThat(watchStockRepository.findByWatchList(watchList)).isEmpty();
	}

	@DisplayName("사용자는 다른 사용자의 watchlists를 삭제할 수 없습니다")
	@Test
	void deleteWatchLists_whenOtherMemberDelete_thenThrowException() {
		// given
		Member member = memberRepository.save(createMember());
		Member hacker = memberRepository.save(createMember("hacker"));
		Stock stock = stockRepository.save(createSamsungStock());
		WatchList watchList = watchListRepository.save(createWatchList(member));
		Long watchListId = watchList.getId();
		watchStockRepository.save(createWatchStock(watchList, stock));

		setAuthentication(hacker);
		// when
		Throwable throwable = catchThrowable(
			() -> watchListService.deleteWatchLists(hacker.getId(), List.of(watchListId)));
		// then
		assertThat(throwable)
			.isInstanceOf(ForbiddenException.class)
			.hasMessage(watchList.toString());
	}

	@DisplayName("회원이 watchlist를 삭제한다.")
	@Test
	void deleteWatchList() {
		// given
		Stock stock = stockRepository.save(createSamsungStock());
		Member member = memberRepository.save(createMember());
		WatchList watchList = watchListRepository.save(createWatchList(member));
		Long watchListId = watchList.getId();
		watchStockRepository.save(createWatchStock(watchList, stock));

		setAuthentication(member);
		// when
		watchListService.deleteWatchList(member.getId(), watchListId);

		// then
		assertThat(watchListRepository.findById(watchListId)).isEmpty();
		assertThat(watchStockRepository.findByWatchList(watchList)).isEmpty();
	}

	@DisplayName("사용자는 다른 사용자의 WatchList를 삭제할 수 없다")
	@Test
	void deleteWatchList_whenOtherMemberWatchList_thenThrowException() {
		// given
		Stock stock = stockRepository.save(createSamsungStock());
		Member member = memberRepository.save(createMember());
		Member hacker = memberRepository.save(createMember("hacker"));
		WatchList watchList = watchListRepository.save(createWatchList(member));
		Long watchListId = watchList.getId();
		watchStockRepository.save(createWatchStock(watchList, stock));

		setAuthentication(hacker);
		// when
		Throwable throwable = catchThrowable(() -> watchListService.deleteWatchList(hacker.getId(), watchListId));

		// then
		assertThat(throwable)
			.isInstanceOf(ForbiddenException.class)
			.hasMessage(watchList.toString());
	}

	@DisplayName("회원이 watchlist에서 종목을 여러개 삭제한다.")
	@Test
	void deleteWatchStocks() {
		// given
		Stock stock = stockRepository.save(createSamsungStock());
		Member member = memberRepository.save(createMember());
		WatchList watchList = watchListRepository.save(createWatchList(member));
		Long watchListId = watchList.getId();
		WatchStock watchStock = watchStockRepository.save(createWatchStock(watchList, stock));
		Long watchStockId = watchStock.getId();
		DeleteWatchStocksRequest request = new DeleteWatchStocksRequest(
			List.of(watchStock.getStock().getTickerSymbol()));

		setAuthentication(member);
		// when
		watchListService.deleteWatchStocks(member.getId(), watchListId, request);

		// then
		assertThat(watchStockRepository.findById(watchStockId)).isEmpty();
	}

	@DisplayName("사용자는 다른 사용자의 watchList의 종목을 삭제할 수 없다")
	@Test
	void deleteWatchStocks_whenOtherMemberDelete_thenThrowException() {
		// given
		Stock stock = stockRepository.save(createSamsungStock());
		Member member = memberRepository.save(createMember());
		Member hacker = memberRepository.save(createMember("hacker"));
		WatchList watchList = watchListRepository.save(createWatchList(member));
		Long watchListId = watchList.getId();
		WatchStock watchStock = watchStockRepository.save(createWatchStock(watchList, stock));
		DeleteWatchStocksRequest request = new DeleteWatchStocksRequest(
			List.of(watchStock.getStock().getTickerSymbol()));

		setAuthentication(hacker);
		// when
		Throwable throwable = catchThrowable(
			() -> watchListService.deleteWatchStocks(hacker.getId(), watchListId, request));
		// then
		assertThat(throwable)
			.isInstanceOf(ForbiddenException.class)
			.hasMessage(watchList.toString());
	}

	@DisplayName("회원이 watchlist에서 종목을 삭제한다.")
	@Test
	void deleteWatchStock() {
		// given
		Stock stock = stockRepository.save(createSamsungStock());
		Member member = memberRepository.save(createMember());
		WatchList watchList = watchListRepository.save(createWatchList(member));
		Long watchListId = watchList.getId();
		WatchStock watchStock = watchStockRepository.save(createWatchStock(watchList, stock));
		Long watchStockId = watchStock.getId();

		setAuthentication(member);
		// when
		watchListService.deleteWatchStock(member.getId(), watchListId, stock.getTickerSymbol());

		// then
		assertThat(watchStockRepository.findById(watchStockId)).isEmpty();
	}

	@SuppressWarnings("checkstyle:NoWhitespaceBefore")
	@DisplayName("사용자는 다른 사용자의 관심 종목을 삭제할 수 없다")
	@Test
	void deleteWatchStock_whenOtherMemberDelete_thenThrowException() {
		// given
		Stock stock = stockRepository.save(createSamsungStock());
		Member member = memberRepository.save(createMember());
		Member hacker = memberRepository.save(createMember("hacker"));
		WatchList watchList = watchListRepository.save(createWatchList(member));
		Long watchListId = watchList.getId();
		watchStockRepository.save(createWatchStock(watchList, stock));

		setAuthentication(hacker);
		// when
		Throwable throwable = catchThrowable(
			() -> watchListService.deleteWatchStock(hacker.getId(), watchListId, stock.getTickerSymbol()));
		// then
		assertThat(throwable)
			.isInstanceOf(ForbiddenException.class)
			.hasMessage(watchList.toString());
	}

	@DisplayName("회원이 watchlist의 이름을 수정한다.")
	@Test
	void changeWatchListName() {
		// given
		Member member = memberRepository.save(createMember());
		WatchList watchList = watchListRepository.save(createWatchList(member));
		Long watchListId = watchList.getId();
		String name = "New Name";
		ChangeWatchListNameRequest request = new ChangeWatchListNameRequest(name);

		setAuthentication(member);
		// when
		watchListService.changeWatchListName(member.getId(), watchListId, request);

		// then
		WatchList findWatchList = watchListRepository.findById(watchListId).orElseThrow();
		assertThat(findWatchList.getName()).isEqualTo(name);
	}

	@DisplayName("사용자는 다른 사용자의 WatchList의 이름을 수정할 수 없다")
	@Test
	void changeWatchListName_whenOtherMemberModify_thenThrowException() {
		// given
		Member member = memberRepository.save(createMember());
		Member hacker = memberRepository.save(createMember("hacker"));
		WatchList watchList = watchListRepository.save(createWatchList(member));
		Long watchListId = watchList.getId();
		String name = "New Name";
		ChangeWatchListNameRequest request = new ChangeWatchListNameRequest(name);

		setAuthentication(hacker);
		// when
		Throwable throwable = catchThrowable(
			() -> watchListService.changeWatchListName(hacker.getId(), watchListId, request));
		//then
		assertThat(throwable)
			.isInstanceOf(ForbiddenException.class)
			.hasMessage(watchList.toString());
	}

	@DisplayName("회원이 watchlist들이 주식을 포함하고 있는지 조회한다.")
	@Test
	void hasStock() {
		// given
		Member member = memberRepository.save(createMember());
		Stock stock = stockRepository.save(createSamsungStock());
		WatchList watchList1 = watchListRepository.save(createWatchList(member));
		watchStockRepository.save(createWatchStock(watchList1, stock));

		WatchList watchList2 = watchListRepository.save(createWatchList(member));

		// when
		List<WatchListHasStockResponse> responseList = watchListService.hasStock(member.getId(),
			stock.getTickerSymbol());

		// then
		assertThat(responseList).hasSize(2);

		boolean hasStockForWatchList1 = false;
		boolean hasStockForWatchList2 = false;

		for (WatchListHasStockResponse response : responseList) {
			if (response.getId() == watchList1.getId()) {
				hasStockForWatchList1 = response.isHasStock();
			} else if (response.getId() == watchList2.getId()) {
				hasStockForWatchList2 = response.isHasStock();
			}
		}

		assertThat(hasStockForWatchList1).isTrue();
		assertThat(hasStockForWatchList2).isFalse();
	}
}
