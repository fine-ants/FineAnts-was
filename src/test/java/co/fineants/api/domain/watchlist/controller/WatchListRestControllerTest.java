package co.fineants.api.domain.watchlist.controller;

import static co.fineants.api.global.success.WatchListSuccessCode.*;
import static org.hamcrest.Matchers.*;
import static org.mockito.BDDMockito.any;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.BDDMockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import co.fineants.AbstractContainerBaseTest;
import co.fineants.TestDataFactory;
import co.fineants.api.domain.kis.repository.ClosingPriceRepository;
import co.fineants.api.domain.kis.repository.PriceRepository;
import co.fineants.api.domain.stock.domain.entity.Stock;
import co.fineants.api.domain.stock.repository.StockRepository;
import co.fineants.api.domain.watchlist.domain.dto.request.ChangeWatchListNameRequest;
import co.fineants.api.domain.watchlist.domain.dto.request.CreateWatchListRequest;
import co.fineants.api.domain.watchlist.domain.dto.request.CreateWatchStockRequest;
import co.fineants.api.domain.watchlist.domain.dto.request.DeleteWatchListsRequests;
import co.fineants.api.domain.watchlist.domain.dto.request.DeleteWatchStocksRequest;
import co.fineants.api.domain.watchlist.domain.dto.response.WatchListHasStockResponse;
import co.fineants.api.domain.watchlist.domain.entity.WatchList;
import co.fineants.api.domain.watchlist.domain.entity.WatchStock;
import co.fineants.api.domain.watchlist.repository.WatchListRepository;
import co.fineants.api.domain.watchlist.repository.WatchStockRepository;
import co.fineants.api.domain.watchlist.service.WatchListService;
import co.fineants.api.global.common.time.LocalDateTimeService;
import co.fineants.api.global.util.ObjectMapperUtil;
import co.fineants.member.domain.Member;
import co.fineants.member.domain.MemberRepository;

class WatchListRestControllerTest extends AbstractContainerBaseTest {

	@Autowired
	private WatchListService mockedWatchListService;

	@Autowired
	private WatchListRestController controller;

	@Autowired
	private MemberRepository memberRepository;

	@Autowired
	private WatchListRepository watchListRepository;

	@Autowired
	private WatchStockRepository watchStockRepository;

	@Autowired
	private StockRepository stockRepository;

	@Autowired
	private PriceRepository priceRepository;

	@Autowired
	private ClosingPriceRepository closingPriceRepository;

	@Autowired
	private LocalDateTimeService spyLocalDateTimeService;

	private MockMvc mockMvc;
	private Member member;
	private Stock stock;

	@BeforeEach
	void setUp() {
		mockMvc = createMockMvc(controller);
		member = memberRepository.save(TestDataFactory.createMember());
		Stock samsung = TestDataFactory.createSamsungStock();
		TestDataFactory.createSamsungStockDividends().forEach(samsung::addStockDividend);
		this.stock = stockRepository.save(samsung);
		priceRepository.savePrice(this.stock, 60000L);
		closingPriceRepository.addPrice(this.stock.getTickerSymbol(), 50000L);

		BDDMockito.given(spyLocalDateTimeService.getLocalDateWithNow())
			.willReturn(LocalDate.of(2023, 1, 1));
	}

	@DisplayName("사용자가 watchlist를 추가한다.")
	@Test
	void createWatchList() throws Exception {
		// given
		CreateWatchListRequest request = new CreateWatchListRequest("My watchlist");

		// when & then
		mockMvc.perform(post("/api/watchlists")
				.contentType(MediaType.APPLICATION_JSON)
				.content(ObjectMapperUtil.serialize(request)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("code").value(equalTo(HttpStatus.OK.value())))
			.andExpect(jsonPath("status").value(equalTo(HttpStatus.OK.getReasonPhrase())))
			.andExpect(jsonPath("message").value(equalTo(CREATED_WATCH_LIST.getMessage())))
			.andExpect(jsonPath("data.watchlistId").value(greaterThan(0)));
	}

	@DisplayName("사용자가 watchlist 목록을 조회한다.")
	@Test
	void readWatchLists() throws Exception {
		// given
		WatchList watchList1 = watchListRepository.save(TestDataFactory.createWatchList("My WatchList 1", member));
		WatchList watchList2 = watchListRepository.save(TestDataFactory.createWatchList("My WatchList 2", member));

		// when & then
		mockMvc.perform(get("/api/watchlists")
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(jsonPath("code").value(equalTo(HttpStatus.OK.value())))
			.andExpect(jsonPath("status").value(equalTo(HttpStatus.OK.getReasonPhrase())))
			.andExpect(jsonPath("message").value(equalTo(READ_WATCH_LISTS.getMessage())))
			.andExpect(jsonPath("data[0].id").value(watchList1.getId().intValue()))
			.andExpect(jsonPath("data[0].name").value("My WatchList 1"))
			.andExpect(jsonPath("data[1].id").value(watchList2.getId().intValue()))
			.andExpect(jsonPath("data[1].name").value("My WatchList 2"));
	}

	@DisplayName("사용자가 watchlist 단일 조회를 한다.")
	@Test
	void readWatchList() throws Exception {
		// given
		WatchList watchList = watchListRepository.save(TestDataFactory.createWatchList("My WatchList 1", member));
		WatchStock watchStock = watchStockRepository.save(TestDataFactory.createWatchStock(stock, watchList));

		// when & then
		mockMvc.perform(get("/api/watchlists/{watchlistId}", watchList.getId())
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(jsonPath("code").value(equalTo(HttpStatus.OK.value())))
			.andExpect(jsonPath("status").value(equalTo(HttpStatus.OK.getReasonPhrase())))
			.andExpect(jsonPath("message").value(equalTo(READ_WATCH_LIST.getMessage())))
			.andExpect(jsonPath("data.watchStocks[0].id").value(equalTo(watchStock.getId().intValue())))
			.andExpect(jsonPath("data.watchStocks[0].companyName").value(equalTo(stock.getCompanyName())))
			.andExpect(jsonPath("data.watchStocks[0].tickerSymbol").value(equalTo(stock.getTickerSymbol())))
			.andExpect(jsonPath("data.watchStocks[0].currentPrice").value(equalTo(60000)))
			.andExpect(jsonPath("data.watchStocks[0].dailyChange").value(equalTo(10000)))
			.andExpect(jsonPath("data.watchStocks[0].dailyChangeRate").value(equalTo(20.0)))
			.andExpect(jsonPath("data.watchStocks[0].annualDividendYield").value(equalTo(2.41)))
			.andExpect(jsonPath("data.watchStocks[0].sector").value(equalTo(stock.getSector())))
			.andExpect(jsonPath("data.watchStocks[0].dateAdded").value(notNullValue()));
	}

	@DisplayName("사용자가 watchlist에 종목을 추가한다.")
	@Test
	void createWatchStocks() throws Exception {
		// given
		WatchList watchList = watchListRepository.save(TestDataFactory.createWatchList("My WatchList 1", member));
		CreateWatchStockRequest request = new CreateWatchStockRequest(List.of("005930"));

		// when & then
		mockMvc.perform(post("/api/watchlists/{watchlistId}/stock", watchList.getId())
				.contentType(MediaType.APPLICATION_JSON)
				.content(ObjectMapperUtil.serialize(request)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("code").value(equalTo(HttpStatus.OK.value())))
			.andExpect(jsonPath("status").value(equalTo(HttpStatus.OK.getReasonPhrase())))
			.andExpect(jsonPath("message").value(equalTo(CREATED_WATCH_STOCK.getMessage())))
			.andExpect(jsonPath("data").value(nullValue()));
	}

	@DisplayName("사용자가 watchlist를 삭제한다.")
	@Test
	void deleteWatchLists() throws Exception {
		// given
		WatchList watchList1 = watchListRepository.save(TestDataFactory.createWatchList("My WatchList 1", member));
		WatchList watchList2 = watchListRepository.save(TestDataFactory.createWatchList("My WatchList 2", member));

		DeleteWatchListsRequests requests = new DeleteWatchListsRequests(List.of(
			watchList1.getId(),
			watchList2.getId()
		));

		// when & then
		mockMvc.perform(delete("/api/watchlists")
				.contentType(MediaType.APPLICATION_JSON)
				.content(ObjectMapperUtil.serialize(requests)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("code").value(equalTo(HttpStatus.OK.value())))
			.andExpect(jsonPath("status").value(equalTo(HttpStatus.OK.getReasonPhrase())))
			.andExpect(jsonPath("message").value(equalTo(DELETED_WATCH_LIST.getMessage())))
			.andExpect(jsonPath("data").value(nullValue()));
	}

	@DisplayName("사용자가 관심종목을 삭제할때 유효하지 않은 입력으로 삭제하지 못한다")
	@ParameterizedTest
	@MethodSource(value = "co.fineants.TestDataProvider#invalidDeleteWatchListIds")
	void deleteWatchLists_whenInvalidInput_thenNotDeleteData(List<Long> watchListIds,
		String[] expectedDefaultMessages) throws Exception {
		// given
		DeleteWatchListsRequests requests = new DeleteWatchListsRequests(watchListIds);

		// when & then
		mockMvc.perform(delete("/api/watchlists")
				.contentType(MediaType.APPLICATION_JSON)
				.content(ObjectMapperUtil.serialize(requests)))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("code").value(equalTo(HttpStatus.BAD_REQUEST.value())))
			.andExpect(jsonPath("status").value(equalTo(HttpStatus.BAD_REQUEST.getReasonPhrase())))
			.andExpect(jsonPath("message").value(equalTo("잘못된 입력형식입니다")))
			.andExpect(jsonPath("data").isArray())
			.andExpect(jsonPath("data[*].field", containsInAnyOrder("watchlistIds")))
			.andExpect(jsonPath("data[*].defaultMessage", containsInAnyOrder(expectedDefaultMessages)));
	}

	@DisplayName("사용자가 watchlist에서 종목을 여러개 삭제한다.")
	@Test
	void deleteWatchStocks() throws Exception {
		// given
		WatchList watchList = watchListRepository.save(TestDataFactory.createWatchList("My WatchList 1", member));
		watchStockRepository.save(TestDataFactory.createWatchStock(stock, watchList));
		DeleteWatchStocksRequest request = new DeleteWatchStocksRequest(List.of("005930"));

		// when & then
		mockMvc.perform(delete("/api/watchlists/{watchlistId}/stock", watchList.getId())
				.contentType(MediaType.APPLICATION_JSON)
				.content(ObjectMapperUtil.serialize(request)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("code").value(equalTo(HttpStatus.OK.value())))
			.andExpect(jsonPath("status").value(equalTo(HttpStatus.OK.getReasonPhrase())))
			.andExpect(jsonPath("message").value(equalTo(DELETED_WATCH_STOCK.getMessage())))
			.andExpect(jsonPath("data").value(nullValue()));
	}

	@DisplayName("사용자가 watchlist에서 종목을 삭제한다.")
	@Test
	void deleteWatchStock() throws Exception {
		// given
		doNothing().when(mockedWatchListService)
			.deleteWatchStocks(anyLong(), any(Long.class), any(DeleteWatchStocksRequest.class));

		// when & then
		mockMvc.perform(delete("/api/watchlists/1/stock/\"005930\"")
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(jsonPath("code").value(equalTo(200)))
			.andExpect(jsonPath("status").value(equalTo("OK")))
			.andExpect(jsonPath("message").value(equalTo("관심목록 종목이 삭제되었습니다")))
			.andExpect(jsonPath("data").value(equalTo(null)));
	}

	@DisplayName("사용자가 watchlist 이름을 변경한다.")
	@Test
	void changeWatchListName() throws Exception {
		Map<String, Object> requestBodyMap = new HashMap<>();
		requestBodyMap.put("name", "My watchlist");
		String body = ObjectMapperUtil.serialize(requestBodyMap);

		doNothing().when(mockedWatchListService)
			.changeWatchListName(anyLong(), any(Long.class), any(ChangeWatchListNameRequest.class));

		// when & then
		mockMvc.perform(put("/api/watchlists/1")
				.contentType(MediaType.APPLICATION_JSON)
				.content(body))
			.andExpect(status().isOk())
			.andExpect(jsonPath("code").value(equalTo(200)))
			.andExpect(jsonPath("status").value(equalTo("OK")))
			.andExpect(jsonPath("message").value(equalTo("관심종목 목록 이름이 변경되었습니다")))
			.andExpect(jsonPath("data").value(equalTo(null)));
	}

	@DisplayName("사용자가 모든 watchlist에 대해 tickerSymbol 보유 여부롤 조회한다.")
	@Test
	void watchListHasStock() throws Exception {
		// given
		String tickerSymbol = "005930";
		List<WatchListHasStockResponse> response = List.of(
			WatchListHasStockResponse.create(1L, "My WatchList1", true),
			WatchListHasStockResponse.create(2L, "My WatchList2", false)
		);
		given(mockedWatchListService.hasStock(anyLong(), any(String.class))).willReturn(response);

		// when & then
		mockMvc.perform(get("/api/watchlists/stockExists/" + tickerSymbol)
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(jsonPath("code").value(equalTo(200)))
			.andExpect(jsonPath("status").value(equalTo("OK")))
			.andExpect(jsonPath("message").value(equalTo("관심목록의 주식 포함 여부 조회가 완료되었습니다")))
			.andExpect(jsonPath("data[0].id").value(equalTo(1)))
			.andExpect(jsonPath("data[0].name").value(equalTo("My WatchList1")))
			.andExpect(jsonPath("data[0].hasStock").value(equalTo(true)))
			.andExpect(jsonPath("data[1].id").value(equalTo(2)))
			.andExpect(jsonPath("data[1].name").value(equalTo("My WatchList2")))
			.andExpect(jsonPath("data[1].hasStock").value(equalTo(false)));
	}
}
