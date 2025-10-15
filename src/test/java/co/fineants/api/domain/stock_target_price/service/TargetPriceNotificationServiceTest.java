package co.fineants.api.domain.stock_target_price.service;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import co.fineants.AbstractContainerBaseTest;
import co.fineants.api.domain.stock.domain.entity.Stock;
import co.fineants.api.domain.stock.repository.StockRepository;
import co.fineants.api.domain.stock_target_price.domain.dto.response.TargetPriceNotificationDeleteResponse;
import co.fineants.api.domain.stock_target_price.domain.entity.StockTargetPrice;
import co.fineants.api.domain.stock_target_price.domain.entity.TargetPriceNotification;
import co.fineants.api.domain.stock_target_price.repository.StockTargetPriceRepository;
import co.fineants.api.domain.stock_target_price.repository.TargetPriceNotificationRepository;
import co.fineants.api.global.errors.exception.business.ForbiddenException;
import co.fineants.api.global.errors.exception.business.StockNotFoundException;
import co.fineants.api.global.errors.exception.business.TargetPriceNotificationNotFoundException;
import co.fineants.member.domain.Member;
import co.fineants.member.domain.MemberRepository;

class TargetPriceNotificationServiceTest extends AbstractContainerBaseTest {

	@Autowired
	private TargetPriceNotificationService service;

	@Autowired
	private MemberRepository memberRepository;

	@Autowired
	private StockRepository stockRepository;

	@Autowired
	private StockTargetPriceRepository repository;

	@Autowired
	private TargetPriceNotificationRepository targetPriceNotificationRepository;

	@DisplayName("사용자는 종목 지정가 알림을 제거합니다")
	@Test
	void deleteStockTargetPriceNotification() {
		// given
		Member member = memberRepository.save(createMember());
		Stock stock = stockRepository.save(createSamsungStock());
		StockTargetPrice stockTargetPrice = repository.save(createStockTargetPrice(member, stock));
		TargetPriceNotification targetPriceNotification = targetPriceNotificationRepository.save(
			createTargetPriceNotification(stockTargetPrice));

		Long id = targetPriceNotification.getId();

		setAuthentication(member);
		// when
		TargetPriceNotificationDeleteResponse response = service.deleteStockTargetPriceNotification(id);

		// then
		assertAll(
			() -> assertThat(response)
				.extracting("deletedIds")
				.asList()
				.hasSize(1),
			() -> assertThat(targetPriceNotificationRepository.findById(id)).isEmpty(),
			() -> assertThat(repository.findById(stockTargetPrice.getId())).isEmpty()
		);
	}

	@DisplayName("사용자는 존재하지 않는 지정가를 삭제할 수 없습니다")
	@Test
	void deleteStockTargetPriceNotification_whenNotExistTargetPriceNotificationIds_thenThrow404Error() {
		// given
		Member member = memberRepository.save(createMember());
		Stock stock = stockRepository.save(createSamsungStock());
		StockTargetPrice stockTargetPrice = repository.save(createStockTargetPrice(member, stock));
		targetPriceNotificationRepository.save(createTargetPriceNotification(stockTargetPrice));

		Long id = 9999L;

		setAuthentication(member);
		// when
		Throwable throwable = catchThrowable(() -> service.deleteStockTargetPriceNotification(id));

		// then
		assertThat(throwable)
			.isInstanceOf(TargetPriceNotificationNotFoundException.class)
			.hasMessage(List.of(id).toString());
	}

	@DisplayName("사용자는 다른 사용자의 지정가 알림을 삭제할 수 없습니다.")
	@Test
	void deleteStockTargetPriceNotification_whenForbiddenTargetPriceNotificationIds_thenThrow403Error() {
		// given
		Member member = memberRepository.save(createMember());
		Stock stock = stockRepository.save(createSamsungStock());
		StockTargetPrice stockTargetPrice = repository.save(createStockTargetPrice(member, stock));
		TargetPriceNotification targetPriceNotification = targetPriceNotificationRepository.save(
			createTargetPriceNotification(stockTargetPrice));

		Member hacker = memberRepository.save(createMember("일개미4567", "kim2@gmail.com"));

		setAuthentication(hacker);
		// when
		Throwable throwable = catchThrowable(
			() -> service.deleteStockTargetPriceNotification(targetPriceNotification.getId()));

		// then
		assertThat(throwable)
			.isInstanceOf(ForbiddenException.class)
			.hasMessage(targetPriceNotification.toString());
	}

	@DisplayName("사용자는 종목 지정가 알림 전체를 삭제합니다")
	@Test
	void deleteAllStockTargetPriceNotification() {
		// given
		Member member = memberRepository.save(createMember());
		Stock stock = stockRepository.save(createSamsungStock());
		StockTargetPrice stockTargetPrice = repository.save(createStockTargetPrice(member, stock));
		List<TargetPriceNotification> targetPriceNotifications = targetPriceNotificationRepository.saveAll(
			createTargetPriceNotification(stockTargetPrice, List.of(60000L, 70000L)));

		List<Long> ids = targetPriceNotifications.stream()
			.map(TargetPriceNotification::getId)
			.toList();

		setAuthentication(member);
		// when
		TargetPriceNotificationDeleteResponse response = service.deleteAllStockTargetPriceNotification(
			ids,
			stock.getTickerSymbol(),
			member.getId()
		);

		// then
		assertAll(
			() -> assertThat(response)
				.extracting("deletedIds")
				.asList()
				.hasSize(2),
			() -> assertThat(targetPriceNotificationRepository.findAllById(ids)).isEmpty(),
			() -> assertThat(repository.findById(stockTargetPrice.getId())).isEmpty()
		);
	}

	@DisplayName("사용자는 종목 지정가 알림을 전체 삭제할 때, 존재하지 않는 지정가 알림을 제거할 수 없습니다.")
	@Test
	void deleteAllStockTargetPriceNotification_whenNotExistTargetPriceNotificationIds_thenThrow404Error() {
		// given
		Member member = memberRepository.save(createMember());
		Stock stock = stockRepository.save(createSamsungStock());
		StockTargetPrice stockTargetPrice = repository.save(createStockTargetPrice(member, stock));
		List<TargetPriceNotification> targetPriceNotifications = targetPriceNotificationRepository.saveAll(
			createTargetPriceNotification(stockTargetPrice, List.of(60000L, 70000L)));

		List<Long> ids = targetPriceNotifications.stream()
			.map(TargetPriceNotification::getId)
			.collect(Collectors.toList());
		ids.add(9999L);

		setAuthentication(member);
		// when
		Throwable throwable = catchThrowable(() -> service.deleteAllStockTargetPriceNotification(
			ids,
			stock.getTickerSymbol(),
			member.getId()
		));

		// then
		assertThat(throwable)
			.isInstanceOf(TargetPriceNotificationNotFoundException.class)
			.hasMessage(ids.toString());
	}

	@DisplayName("사용자는 종목 지정가 알림을 전체 삭제할 때, 존재하지 않는 종목에 대해서 제거할 수 없습니다.")
	@Test
	void deleteAllStockTargetPriceNotification_whenNotExistStock_thenThrow404Error() {
		// given
		Member member = memberRepository.save(createMember());
		Stock stock = stockRepository.save(createSamsungStock());
		StockTargetPrice stockTargetPrice = repository.save(createStockTargetPrice(member, stock));
		List<TargetPriceNotification> targetPriceNotifications = targetPriceNotificationRepository.saveAll(
			createTargetPriceNotification(stockTargetPrice, List.of(60000L, 70000L)));

		List<Long> ids = targetPriceNotifications.stream()
			.map(TargetPriceNotification::getId)
			.toList();

		setAuthentication(member);
		// when
		Throwable throwable = catchThrowable(() -> service.deleteAllStockTargetPriceNotification(
			ids,
			"999999",
			member.getId()
		));

		// then
		assertThat(throwable)
			.isInstanceOf(StockNotFoundException.class)
			.hasMessage("999999");
	}

	@DisplayName("사용자는 종목 지정가 알림을 전체 삭제할 때, 다른 사용자의 지정가 알림을 삭제할 수 없습니다")
	@Test
	void deleteAllStockTargetPriceNotification_whenForbiddenTargetPriceNotifications_thenThrow403Error() {
		// given
		Member member = memberRepository.save(createMember());
		Member hacker = memberRepository.save(createMember("hacker"));
		Stock stock = stockRepository.save(createSamsungStock());
		StockTargetPrice stockTargetPrice = repository.save(createStockTargetPrice(member, stock));
		List<TargetPriceNotification> targetPriceNotifications = targetPriceNotificationRepository.saveAll(
			createTargetPriceNotification(stockTargetPrice, List.of(60000L, 70000L)));

		List<Long> ids = targetPriceNotifications.stream()
			.map(TargetPriceNotification::getId)
			.toList();

		setAuthentication(hacker);
		// when
		Throwable throwable = catchThrowable(() -> service.deleteAllStockTargetPriceNotification(
			ids,
			stock.getTickerSymbol(),
			hacker.getId()
		));

		// then
		assertThat(throwable)
			.isInstanceOf(ForbiddenException.class)
			.hasMessage(targetPriceNotifications.get(0).toString());
	}

}
