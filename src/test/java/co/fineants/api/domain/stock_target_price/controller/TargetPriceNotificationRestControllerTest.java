package co.fineants.api.domain.stock_target_price.controller;

import static co.fineants.api.global.success.StockSuccessCode.*;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import co.fineants.AbstractContainerBaseTest;
import co.fineants.TestDataFactory;
import co.fineants.api.domain.common.money.Money;
import co.fineants.stock.domain.Stock;
import co.fineants.stock.infrastructure.StockSpringDataJpaRepository;
import co.fineants.api.domain.stock_target_price.domain.dto.request.TargetPriceNotificationDeleteRequest;
import co.fineants.api.domain.stock_target_price.domain.entity.StockTargetPrice;
import co.fineants.api.domain.stock_target_price.domain.entity.TargetPriceNotification;
import co.fineants.api.domain.stock_target_price.repository.StockTargetPriceRepository;
import co.fineants.api.domain.stock_target_price.repository.TargetPriceNotificationRepository;
import co.fineants.api.domain.stock_target_price.service.TargetPriceNotificationService;
import co.fineants.api.global.util.ObjectMapperUtil;
import co.fineants.member.domain.Member;
import co.fineants.member.domain.MemberRepository;

class TargetPriceNotificationRestControllerTest extends AbstractContainerBaseTest {

	@Autowired
	private TargetPriceNotificationService mockedTargetPriceNotificationService;

	@Autowired
	private TargetPriceNotificationRestController controller;

	@Autowired
	private MemberRepository memberRepository;

	@Autowired
	private StockSpringDataJpaRepository stockRepository;

	@Autowired
	private StockTargetPriceRepository stockTargetPriceRepository;

	@Autowired
	private TargetPriceNotificationRepository targetPriceNotificationRepository;

	private MockMvc mockMvc;
	private Stock stock;
	private StockTargetPrice stockTargetPrice;

	@BeforeEach
	void setUp() {
		mockMvc = createMockMvc(controller);
		Member member = memberRepository.save(TestDataFactory.createMember());
		stock = stockRepository.save(TestDataFactory.createSamsungStock());
		stockTargetPrice = stockTargetPriceRepository.save(
			StockTargetPrice.newStockTargetPriceWithActive(member, stock));
	}

	@DisplayName("사용자는 종목 지정가 알림들을 삭제합니다")
	@Test
	void deleteTargetPriceNotifications() throws Exception {
		// given
		TargetPriceNotification targetPriceNotification1 = targetPriceNotificationRepository.save(
			TargetPriceNotification.newTargetPriceNotification(
				Money.won(60000L), stockTargetPrice));
		TargetPriceNotification targetPriceNotification2 = targetPriceNotificationRepository.save(
			TargetPriceNotification.newTargetPriceNotification(
				Money.won(70000L), stockTargetPrice));

		String tickerSymbol = stock.getTickerSymbol();
		List<Long> targetPriceNotificationIds = List.of(targetPriceNotification1.getId(),
			targetPriceNotification2.getId());
		TargetPriceNotificationDeleteRequest request = new TargetPriceNotificationDeleteRequest(tickerSymbol,
			targetPriceNotificationIds);

		// when & then
		mockMvc.perform(delete("/api/stocks/target-price/notifications")
				.contentType(MediaType.APPLICATION_JSON)
				.content(ObjectMapperUtil.serialize(request)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("code").value(equalTo(HttpStatus.OK.value())))
			.andExpect(jsonPath("status").value(equalTo(HttpStatus.OK.getReasonPhrase())))
			.andExpect(jsonPath("message").value(equalTo(OK_DELETE_TARGET_PRICE_NOTIFICATIONS.getMessage())))
			.andExpect(jsonPath("data").value(nullValue()));
	}

	@DisplayName("사용자는 유효하지 않은 입력 형식으로 종목 지정가를 삭제할 수 없다")
	@MethodSource(value = "co.fineants.TestDataProvider#invalidTargetPriceNotificationIds")
	@ParameterizedTest
	void deleteTargetPriceNotifications_whenInvalidInput_thenNotDeleteData(
		String tickerSymbol,
		List<Long> targetPriceNotificationIds,
		String[] expectedDefaultMessages)
		throws Exception {
		// given
		TargetPriceNotificationDeleteRequest request = new TargetPriceNotificationDeleteRequest(tickerSymbol,
			targetPriceNotificationIds);

		// when & then
		mockMvc.perform(delete("/api/stocks/target-price/notifications")
				.contentType(MediaType.APPLICATION_JSON)
				.content(ObjectMapperUtil.serialize(request)))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("code").value(equalTo(HttpStatus.BAD_REQUEST.value())))
			.andExpect(jsonPath("status").value(equalTo(HttpStatus.BAD_REQUEST.getReasonPhrase())))
			.andExpect(jsonPath("message").value(equalTo("잘못된 입력형식입니다")))
			.andExpect(jsonPath("data").isArray())
			.andExpect(jsonPath("data[*].field", containsInAnyOrder(
				"tickerSymbol",
				"targetPriceNotificationIds"
			)))
			.andExpect(jsonPath("data[*].defaultMessage", containsInAnyOrder(expectedDefaultMessages)));
	}

	@DisplayName("사용자는 종목 지정가 알림을 삭제합니다")
	@Test
	void deleteTargetPriceNotification() throws Exception {
		// given
		TargetPriceNotification targetPriceNotification1 = targetPriceNotificationRepository.save(
			TargetPriceNotification.newTargetPriceNotification(
				Money.won(60000L), stockTargetPrice));

		Long targetPriceNotificationId = targetPriceNotification1.getId();
		// when & then
		mockMvc.perform(
				delete("/api/stocks/target-price/notifications/{targetPriceNotificationId}",
					targetPriceNotificationId))
			.andExpect(status().isOk())
			.andExpect(jsonPath("code").value(equalTo(HttpStatus.OK.value())))
			.andExpect(jsonPath("status").value(equalTo(HttpStatus.OK.getReasonPhrase())))
			.andExpect(jsonPath("message").value(equalTo(OK_DELETE_TARGET_PRICE_NOTIFICATIONS.getMessage())))
			.andExpect(jsonPath("data").value(nullValue()));
	}
}
