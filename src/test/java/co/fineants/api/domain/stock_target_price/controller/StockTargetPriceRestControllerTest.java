package co.fineants.api.domain.stock_target_price.controller;

import static co.fineants.api.global.success.StockSuccessCode.*;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.anyLong;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.HashMap;
import java.util.Map;

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
import co.fineants.api.domain.kis.repository.ClosingPriceRepository;
import co.fineants.api.domain.stock.domain.entity.Stock;
import co.fineants.api.domain.stock.repository.StockRepository;
import co.fineants.api.domain.stock_target_price.domain.dto.request.TargetPriceNotificationCreateRequest;
import co.fineants.api.domain.stock_target_price.domain.dto.request.TargetPriceNotificationUpdateRequest;
import co.fineants.api.domain.stock_target_price.domain.dto.response.TargetPriceNotificationUpdateResponse;
import co.fineants.api.domain.stock_target_price.domain.entity.StockTargetPrice;
import co.fineants.api.domain.stock_target_price.domain.entity.TargetPriceNotification;
import co.fineants.api.domain.stock_target_price.repository.StockTargetPriceRepository;
import co.fineants.api.domain.stock_target_price.repository.TargetPriceNotificationRepository;
import co.fineants.api.domain.stock_target_price.service.StockTargetPriceService;
import co.fineants.api.global.util.ObjectMapperUtil;
import co.fineants.member.domain.Member;
import co.fineants.member.domain.MemberRepository;

class StockTargetPriceRestControllerTest extends AbstractContainerBaseTest {

	@Autowired
	private StockTargetPriceService mockedStockTargetPriceService;

	@Autowired
	private StockTargetPriceRestController controller;

	@Autowired
	private MemberRepository memberRepository;

	@Autowired
	private StockRepository stockRepository;

	@Autowired
	private StockTargetPriceRepository stockTargetPriceRepository;

	@Autowired
	private TargetPriceNotificationRepository targetPriceNotificationRepository;

	@Autowired
	private ClosingPriceRepository closingPriceRepository;

	private MockMvc mockMvc;

	@BeforeEach
	void setUp() {
		mockMvc = createMockMvc(controller);
		closingPriceRepository.addPrice("005930", 50000L);
	}

	@DisplayName("사용자는 종목 지정가 알림을 추가합니다")
	@Test
	void createStockTargetPriceNotification() throws Exception {
		// given
		memberRepository.save(TestDataFactory.createMember());
		stockRepository.save(TestDataFactory.createSamsungStock());

		String tickerSymbol = "005930";
		Money targetPrice = Money.won(60000L);
		TargetPriceNotificationCreateRequest request = new TargetPriceNotificationCreateRequest(tickerSymbol,
			targetPrice);

		// when & then
		mockMvc.perform(post("/api/stocks/target-price/notifications")
				.contentType(MediaType.APPLICATION_JSON)
				.content(ObjectMapperUtil.serialize(request)))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("code").value(equalTo(HttpStatus.CREATED.value())))
			.andExpect(jsonPath("status").value(equalTo(HttpStatus.CREATED.getReasonPhrase())))
			.andExpect(jsonPath("message").value(equalTo(OK_CREATE_TARGET_PRICE_NOTIFICATION.getMessage())))
			.andExpect(jsonPath("data.targetPriceNotificationId").value(greaterThan(0)))
			.andExpect(jsonPath("data.tickerSymbol").value(equalTo(tickerSymbol)));
	}

	@DisplayName("사용자는 유효하지 않은 입력으로 종목 지정가 알림을 추가할 수 없습니다")
	@ParameterizedTest
	@MethodSource(value = "co.fineants.TestDataProvider#invalidTargetPrice")
	void createStockTargetPriceNotification_whenInvalidTargetPrice_thenResponse400Error(String tickerSymbol,
		Money targetPrice) throws
		Exception {
		// given
		memberRepository.save(TestDataFactory.createMember());
		stockRepository.save(TestDataFactory.createSamsungStock());

		TargetPriceNotificationCreateRequest request = new TargetPriceNotificationCreateRequest(tickerSymbol,
			targetPrice);

		// when & then
		mockMvc.perform(post("/api/stocks/target-price/notifications")
				.contentType(MediaType.APPLICATION_JSON)
				.content(ObjectMapperUtil.serialize(request)))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("code").value(equalTo(HttpStatus.BAD_REQUEST.value())))
			.andExpect(jsonPath("status").value(equalTo(HttpStatus.BAD_REQUEST.getReasonPhrase())))
			.andExpect(jsonPath("message").value(equalTo("잘못된 입력형식입니다")))
			.andExpect(jsonPath("data").isArray())
			.andExpect(jsonPath("data[*].field", containsInAnyOrder("tickerSymbol", "targetPrice")))
			.andExpect(jsonPath("data[*].defaultMessage", containsInAnyOrder("필수 정보입니다", "금액은 양수여야 합니다")));
	}

	@DisplayName("사용자는 종목 지정가 알림 목록을 조회합니다")
	@Test
	void searchStockTargetPriceNotification() throws Exception {
		// given
		Member member = memberRepository.save(TestDataFactory.createMember());
		Stock stock = stockRepository.save(TestDataFactory.createSamsungStock());
		StockTargetPrice stockTargetPrice = StockTargetPrice.newStockTargetPriceWithActive(member, stock);
		StockTargetPrice saveStockTargetPrice = stockTargetPriceRepository.save(stockTargetPrice);

		Money targetPrice = Money.won(60000L);
		TargetPriceNotification targetPriceNotification = TargetPriceNotification.newTargetPriceNotification(
			targetPrice, saveStockTargetPrice);
		TargetPriceNotification saveTargetPriceNotification = targetPriceNotificationRepository.save(
			targetPriceNotification);

		// when & then
		mockMvc.perform(get("/api/stocks/target-price/notifications"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("code").value(equalTo(HttpStatus.OK.value())))
			.andExpect(jsonPath("status").value(equalTo(HttpStatus.OK.getReasonPhrase())))
			.andExpect(jsonPath("message").value(equalTo(OK_SEARCH_TARGET_PRICE_NOTIFICATIONS.getMessage())))
			.andExpect(jsonPath("data.stocks[0].companyName").value(equalTo(stock.getCompanyName())))
			.andExpect(jsonPath("data.stocks[0].tickerSymbol").value(equalTo(stock.getTickerSymbol())))
			.andExpect(jsonPath("data.stocks[0].lastPrice").value(equalTo(50000)))
			.andExpect(jsonPath("data.stocks[0].targetPrices[0].notificationId").value(
				equalTo(saveTargetPriceNotification.getId().intValue())))
			.andExpect(jsonPath("data.stocks[0].targetPrices[0].targetPrice").value(equalTo(60_000)))
			.andExpect(jsonPath("data.stocks[0].targetPrices[0].dateAdded").value(notNullValue()))
			.andExpect(jsonPath("data.stocks[0].isActive").value(equalTo(true)))
			.andExpect(jsonPath("data.stocks[0].lastUpdated").value(notNullValue()));
	}

	@DisplayName("사용자는 특정 종목의 지정 알림가들을 조회합니다")
	@Test
	void searchTargetPriceNotifications() throws Exception {
		// given
		Member member = memberRepository.save(TestDataFactory.createMember());
		Stock stock = stockRepository.save(TestDataFactory.createSamsungStock());
		StockTargetPrice stockTargetPrice = StockTargetPrice.newStockTargetPriceWithActive(member, stock);
		StockTargetPrice saveStockTargetPrice = stockTargetPriceRepository.save(stockTargetPrice);

		TargetPriceNotification targetPriceNotification1 = targetPriceNotificationRepository.save(
			TargetPriceNotification.newTargetPriceNotification(
				Money.won(60000L), saveStockTargetPrice));
		TargetPriceNotification targetPriceNotification2 = targetPriceNotificationRepository.save(
			TargetPriceNotification.newTargetPriceNotification(
				Money.won(70000L), saveStockTargetPrice));

		// when & then
		mockMvc.perform(get("/api/stocks/{tickerSymbol}/target-price/notifications", stock.getTickerSymbol()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("code").value(equalTo(HttpStatus.OK.value())))
			.andExpect(jsonPath("status").value(equalTo(HttpStatus.OK.getReasonPhrase())))
			.andExpect(jsonPath("message").value(equalTo(OK_SEARCH_SPECIFIC_TARGET_PRICE_NOTIFICATIONS.getMessage())))
			.andExpect(jsonPath("data.targetPrices[0].notificationId").value(
				equalTo(targetPriceNotification1.getId().intValue())))
			.andExpect(jsonPath("data.targetPrices[0].targetPrice").value(equalTo(60000)))
			.andExpect(jsonPath("data.targetPrices[0].dateAdded").value(notNullValue()))
			.andExpect(jsonPath("data.targetPrices[1].notificationId").value(
				equalTo(targetPriceNotification2.getId().intValue())))
			.andExpect(jsonPath("data.targetPrices[1].targetPrice").value(equalTo(70000)))
			.andExpect(jsonPath("data.targetPrices[1].dateAdded").value(notNullValue()));
	}

	@DisplayName("사용자는 종목 지정가 알림의 정보를 수정한다")
	@Test
	void updateStockTargetPriceNotification() throws Exception {
		// given
		Stock stock = createSamsungStock();
		Map<String, Object> body = Map.of(
			"tickerSymbol", stock.getTickerSymbol(),
			"isActive", false
		);

		given(mockedStockTargetPriceService.updateStockTargetPrice(any(TargetPriceNotificationUpdateRequest.class),
			anyLong()))
			.willReturn(TargetPriceNotificationUpdateResponse.builder()
				.stockTargetPriceId(1L)
				.tickerSymbol(stock.getTickerSymbol())
				.isActive(false)
				.build());

		// when & then
		mockMvc.perform(put("/api/stocks/target-price/notifications")
				.contentType(MediaType.APPLICATION_JSON)
				.content(ObjectMapperUtil.serialize(body)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("code").value(equalTo(200)))
			.andExpect(jsonPath("status").value(equalTo("OK")))
			.andExpect(jsonPath("message").value(equalTo("종목 지정가 알림을 비 활성화하였습니다")));
	}

	@DisplayName("사용자는 유효하지 않은 입력으로 종목 지정가 알림의 정보를  수정할 수 없다")
	@Test
	void updateStockTargetPriceNotification_whenInvalidInput_thenResponse400Error() throws Exception {
		// given
		Map<String, Object> body = new HashMap<>();
		body.put("tickerSymbol", null);
		body.put("isActive", null);

		// when & then
		mockMvc.perform(put("/api/stocks/target-price/notifications")
				.contentType(MediaType.APPLICATION_JSON)
				.content(ObjectMapperUtil.serialize(body)))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("code").value(equalTo(400)))
			.andExpect(jsonPath("status").value(equalTo("Bad Request")))
			.andExpect(jsonPath("message").value(equalTo("잘못된 입력형식입니다")))
			.andExpect(jsonPath("data").isArray());
	}
}
