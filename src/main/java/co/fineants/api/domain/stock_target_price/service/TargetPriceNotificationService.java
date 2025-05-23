package co.fineants.api.domain.stock_target_price.service;

import java.util.List;

import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.fineants.api.domain.stock.repository.StockRepository;
import co.fineants.api.domain.stock_target_price.domain.dto.response.TargetPriceNotificationDeleteResponse;
import co.fineants.api.domain.stock_target_price.domain.entity.TargetPriceNotification;
import co.fineants.api.domain.stock_target_price.repository.StockTargetPriceRepository;
import co.fineants.api.domain.stock_target_price.repository.TargetPriceNotificationRepository;
import co.fineants.api.global.common.authorized.Authorized;
import co.fineants.api.global.common.authorized.service.TargetPriceNotificationAuthorizedService;
import co.fineants.api.global.common.resource.ResourceId;
import co.fineants.api.global.common.resource.ResourceIds;
import co.fineants.api.global.errors.exception.business.StockNotFoundException;
import co.fineants.api.global.errors.exception.business.TargetPriceNotificationNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TargetPriceNotificationService {

	private final StockTargetPriceRepository repository;
	private final TargetPriceNotificationRepository targetPriceNotificationRepository;
	private final StockRepository stockRepository;

	// 종목 지정가 알림 다수 제거
	@Transactional
	@Authorized(serviceClass = TargetPriceNotificationAuthorizedService.class)
	@Secured("ROLE_USER")
	public TargetPriceNotificationDeleteResponse deleteAllStockTargetPriceNotification(
		@ResourceIds List<Long> targetPriceNotificationIds,
		String tickerSymbol,
		Long memberId
	) {
		// 존재하지 않는 종목 검증
		verifyExistStock(tickerSymbol);
		// 존재하지 않는 종목 지정가가 있는지 검증
		verifyExistTargetPriceById(targetPriceNotificationIds);

		int deletedCount = deleteAllTargetPriceNotification(targetPriceNotificationIds);
		int deletedStockTargetPriceCount = deleteStockTargetPrice(tickerSymbol, memberId);
		log.info("종목 지정가 알림 제거 결과 : ids={}, deletedTargetPriceNotificationCount={}, deletedStockTargetPriceCount = {}",
			targetPriceNotificationIds, deletedCount, deletedStockTargetPriceCount);
		return TargetPriceNotificationDeleteResponse.from(targetPriceNotificationIds);
	}

	private void verifyExistStock(String tickerSymbol) {
		if (stockRepository.findByTickerSymbol(tickerSymbol).isEmpty()) {
			throw new StockNotFoundException(tickerSymbol);
		}
	}

	// 종목 지정가 존재 검증
	private void verifyExistTargetPriceById(List<Long> targetPriceNotificationIds) {
		if (targetPriceNotificationRepository.findAllById(targetPriceNotificationIds).size()
			!= targetPriceNotificationIds.size()) {
			throw new TargetPriceNotificationNotFoundException(targetPriceNotificationIds.toString());
		}
	}

	private int deleteAllTargetPriceNotification(List<Long> targetPriceNotificationIds) {
		return targetPriceNotificationRepository.deleteAllByTargetPriceNotificationIds(targetPriceNotificationIds);
	}

	private int deleteStockTargetPrice(String tickerSymbol, Long memberId) {
		return repository.deleteByTickerSymbolAndMemberId(tickerSymbol, memberId);
	}

	// 종목 지정가 알림 단일 제거
	@Transactional
	@Authorized(serviceClass = TargetPriceNotificationAuthorizedService.class)
	@Secured("ROLE_USER")
	public TargetPriceNotificationDeleteResponse deleteStockTargetPriceNotification(
		@ResourceId Long targetPriceNotificationId
	) {
		List<Long> targetPriceNotificationIds = List.of(targetPriceNotificationId);
		// 삭제하고자 하는 종목 지정가가 존재하는지 검증
		verifyExistTargetPriceById(targetPriceNotificationIds);

		TargetPriceNotification targetPriceNotification = findTargetPriceNotification(targetPriceNotificationId);
		int deletedTargetPriceNotificationCount = deleteAllTargetPriceNotification(targetPriceNotificationIds);

		// TargetPriceNotification이 한개도 없으면 StockTargetPrice 제거
		if (targetPriceNotification.getStockTargetPrice().getTargetPriceNotifications().isEmpty()) {
			repository.deleteById(targetPriceNotification.getStockTargetPrice().getId());
		}

		log.info("종목 지정가 알림 제거 결과 : targetPriceNotificationIds={}, deletedTargetPriceNotificationCount={}",
			targetPriceNotificationIds, deletedTargetPriceNotificationCount);
		return TargetPriceNotificationDeleteResponse.from(targetPriceNotificationIds);
	}

	private TargetPriceNotification findTargetPriceNotification(Long targetPriceNotificationId) {
		return targetPriceNotificationRepository.findById(targetPriceNotificationId)
			.orElseThrow(() -> new TargetPriceNotificationNotFoundException(targetPriceNotificationId.toString()));
	}
}
