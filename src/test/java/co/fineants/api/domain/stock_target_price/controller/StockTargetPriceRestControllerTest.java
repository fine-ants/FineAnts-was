package co.fineants.api.domain.stock_target_price.controller;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.anyLong;
import static org.mockito.BDDMockito.anyString;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import co.fineants.api.domain.common.money.Money;
import co.fineants.api.domain.stock.domain.entity.Stock;
import co.fineants.api.domain.stock_target_price.domain.dto.request.TargetPriceNotificationCreateRequest;
import co.fineants.api.domain.stock_target_price.domain.dto.request.TargetPriceNotificationUpdateRequest;
import co.fineants.api.domain.stock_target_price.domain.dto.response.TargetPriceItem;
import co.fineants.api.domain.stock_target_price.domain.dto.response.TargetPriceNotificationCreateResponse;
import co.fineants.api.domain.stock_target_price.domain.dto.response.TargetPriceNotificationSearchItem;
import co.fineants.api.domain.stock_target_price.domain.dto.response.TargetPriceNotificationSearchResponse;
import co.fineants.api.domain.stock_target_price.domain.dto.response.TargetPriceNotificationSpecificItem;
import co.fineants.api.domain.stock_target_price.domain.dto.response.TargetPriceNotificationSpecifiedSearchResponse;
import co.fineants.api.domain.stock_target_price.domain.dto.response.TargetPriceNotificationUpdateResponse;
import co.fineants.api.domain.stock_target_price.service.StockTargetPriceService;
import co.fineants.api.global.util.ObjectMapperUtil;
import co.fineants.support.controller.ControllerTestSupport;

class StockTargetPriceRestControllerTest extends ControllerTestSupport {

	@Autowired
	private StockTargetPriceService mockedStockTargetPriceService;

	@Override
	protected Object initController() {
		return new StockTargetPriceRestController(mockedStockTargetPriceService);
	}

	@DisplayName("사용자는 종목 지정가 알림을 추가합니다")
	@Test
	void createStockTargetPriceNotification() throws Exception {
		// given
		given(mockedStockTargetPriceService.createStockTargetPrice(
			any(TargetPriceNotificationCreateRequest.class),
			anyLong()))
			.willReturn(TargetPriceNotificationCreateResponse.builder()
				.targetPriceNotificationId(1L)
				.tickerSymbol("005930")
				.targetPrice(Money.won(60000L))
				.build());

		String tickerSymbol = "005930";
		Map<String, Object> body = Map.of(
			"tickerSymbol", tickerSymbol,
			"targetPrice", 60000L);

		// when & then
		mockMvc.perform(post("/api/stocks/target-price/notifications")
				.contentType(MediaType.APPLICATION_JSON)
				.content(ObjectMapperUtil.serialize(body)))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("code").value(equalTo(201)))
			.andExpect(jsonPath("status").value(equalTo("Created")))
			.andExpect(jsonPath("message").value(equalTo("해당 종목 지정가 알림을 추가했습니다")))
			.andExpect(jsonPath("data.targetPriceNotificationId").value(equalTo(1)))
			.andExpect(jsonPath("data.tickerSymbol").value(equalTo("005930")));
	}

	@DisplayName("사용자는 유효하지 않은 입력으로 종목 지정가 알림을 추가할 수 없습니다")
	@MethodSource(value = "invalidTargetPrice")
	@ParameterizedTest
	void createStockTargetPriceNotification_whenInvalidTargetPrice_thenResponse400Error(String tickerSymbol,
		Long targetPrice) throws
		Exception {
		// given
		Map<String, Object> body = new HashMap<>();
		body.put("tickerSymbol", tickerSymbol);
		body.put("targetPrice", targetPrice);

		// when & then
		mockMvc.perform(post("/api/stocks/target-price/notifications")
				.contentType(MediaType.APPLICATION_JSON)
				.content(ObjectMapperUtil.serialize(body)))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("code").value(equalTo(400)))
			.andExpect(jsonPath("status").value(equalTo("Bad Request")))
			.andExpect(jsonPath("message").value(equalTo("잘못된 입력형식입니다")))
			.andExpect(jsonPath("data").isArray());
	}

	@DisplayName("사용자는 종목 지정가 알림 목록을 조회합니다")
	@Test
	void searchStockTargetPriceNotification() throws Exception {
		// given
		Stock stock = createSamsungStock();
		LocalDateTime now = LocalDateTime.now();
		given(mockedStockTargetPriceService.searchStockTargetPrices(anyLong()))
			.willReturn(TargetPriceNotificationSearchResponse.builder()
				.stocks(List.of(TargetPriceNotificationSearchItem.builder()
					.companyName(stock.getCompanyName())
					.tickerSymbol(stock.getTickerSymbol())
					.lastPrice(Money.won(50000L))
					.targetPrices(List.of(
						TargetPriceItem.builder()
							.notificationId(1L)
							.targetPrice(Money.won(60000L))
							.dateAdded(now)
							.build(),
						TargetPriceItem.builder()
							.notificationId(2L)
							.targetPrice(Money.won(70000L))
							.dateAdded(now)
							.build()
					))
					.isActive(true)
					.lastUpdated(now)
					.build()))
				.build());

		// when & then
		mockMvc.perform(get("/api/stocks/target-price/notifications"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("code").value(equalTo(200)))
			.andExpect(jsonPath("status").value(equalTo("OK")))
			.andExpect(jsonPath("message").value(equalTo("모든 알림 조회를 성공했습니다")))
			.andExpect(jsonPath("data.stocks[0].companyName").value(equalTo(stock.getCompanyName())))
			.andExpect(jsonPath("data.stocks[0].tickerSymbol").value(equalTo(stock.getTickerSymbol())))
			.andExpect(jsonPath("data.stocks[0].lastPrice").value(equalTo(50000)))
			.andExpect(jsonPath("data.stocks[0].targetPrices[0].notificationId").value(equalTo(1)))
			.andExpect(jsonPath("data.stocks[0].targetPrices[0].targetPrice").value(equalTo(60000)))
			.andExpect(jsonPath("data.stocks[0].targetPrices[0].dateAdded").isNotEmpty())
			.andExpect(jsonPath("data.stocks[0].targetPrices[1].notificationId").value(equalTo(2)))
			.andExpect(jsonPath("data.stocks[0].targetPrices[1].targetPrice").value(equalTo(70000)))
			.andExpect(jsonPath("data.stocks[0].targetPrices[1].dateAdded").isNotEmpty())
			.andExpect(jsonPath("data.stocks[0].isActive").value(equalTo(true)))
			.andExpect(jsonPath("data.stocks[0].lastUpdated").isNotEmpty());
	}

	@DisplayName("사용자는 특정 종목의 지정 알림가들을 조회합니다")
	@Test
	void searchTargetPriceNotifications() throws Exception {
		// given
		Stock stock = createSamsungStock();
		LocalDateTime now = LocalDateTime.now();
		given(mockedStockTargetPriceService.searchStockTargetPrice(anyString(), anyLong()))
			.willReturn(TargetPriceNotificationSpecifiedSearchResponse.builder()
				.targetPrices(List.of(
					TargetPriceNotificationSpecificItem.builder()
						.notificationId(1L)
						.targetPrice(Money.won(60000L))
						.dateAdded(now)
						.build(),
					TargetPriceNotificationSpecificItem.builder()
						.notificationId(2L)
						.targetPrice(Money.won(70000L))
						.dateAdded(now)
						.build()
				))
				.build());

		// when & then
		mockMvc.perform(get("/api/stocks/{tickerSymbol}/target-price/notifications", stock.getTickerSymbol()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("code").value(equalTo(200)))
			.andExpect(jsonPath("status").value(equalTo("OK")))
			.andExpect(jsonPath("message").value(equalTo("종목 지정가 알림 특정 조회를 성공했습니다")))
			.andExpect(jsonPath("data.targetPrices[0].notificationId").value(equalTo(1)))
			.andExpect(jsonPath("data.targetPrices[0].targetPrice").value(equalTo(60000)))
			.andExpect(jsonPath("data.targetPrices[0].dateAdded").isNotEmpty())
			.andExpect(jsonPath("data.targetPrices[1].notificationId").value(equalTo(2)))
			.andExpect(jsonPath("data.targetPrices[1].targetPrice").value(equalTo(70000)))
			.andExpect(jsonPath("data.targetPrices[1].dateAdded").isNotEmpty());
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

	public static Stream<Arguments> invalidTargetPrice() {
		return Stream.of(
			Arguments.of(null, -1L),
			Arguments.of(null, null)
		);
	}
}
