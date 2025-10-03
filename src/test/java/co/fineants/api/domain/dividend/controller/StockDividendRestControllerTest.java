package co.fineants.api.domain.dividend.controller;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.io.InputStream;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.Arrays;
import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;

import co.fineants.AbstractContainerBaseTest;
import co.fineants.api.domain.common.money.Money;
import co.fineants.api.domain.dividend.domain.entity.StockDividend;
import co.fineants.api.domain.dividend.repository.StockDividendRepository;
import co.fineants.api.domain.kis.domain.dto.response.KisDividend;
import co.fineants.api.domain.kis.service.KisService;
import co.fineants.api.domain.stock.domain.entity.Stock;
import co.fineants.api.domain.stock.repository.StockRepository;
import co.fineants.api.global.common.time.LocalDateTimeService;
import co.fineants.api.global.errors.handler.GlobalExceptionHandler;
import co.fineants.api.global.security.oauth.resolver.MemberAuthenticationArgumentResolver;
import co.fineants.api.infra.s3.service.DeleteDividendService;
import co.fineants.api.infra.s3.service.RemoteFileFetcher;
import co.fineants.api.infra.s3.service.WriteDividendService;
import co.fineants.api.infra.s3.service.imple.FileContentComparator;

@WithMockUser(roles = {"ADMIN"})
class StockDividendRestControllerTest extends AbstractContainerBaseTest {

	@Autowired
	protected MemberAuthenticationArgumentResolver mockedMemberAuthenticationArgumentResolver;
	@Autowired
	protected ObjectMapper objectMapper;
	private MockMvc mockMvc;
	@Autowired
	private GlobalExceptionHandler globalExceptionHandler;
	@Autowired
	private StockDividendRestController controller;

	@Autowired
	private StockRepository stockRepository;

	@Autowired
	private StockDividendRepository stockDividendRepository;

	@Autowired
	private RemoteFileFetcher remoteFileFetcher;

	@Value("${aws.s3.dividend-csv-path}")
	private String dividendPath;

	@Autowired
	private DeleteDividendService deleteDividendService;

	@Autowired
	private KisService mockedKisService;

	@Autowired
	private LocalDateTimeService spyLocalDateTimeService;

	@Autowired
	private WriteDividendService writeDividendService;

	@BeforeEach
	void setUp() {
		mockMvc = MockMvcBuilders.standaloneSetup(controller)
			.setControllerAdvice(globalExceptionHandler)
			.setCustomArgumentResolvers(mockedMemberAuthenticationArgumentResolver)
			.setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
			.alwaysDo(print())
			.build();

		Stock samsung = stockRepository.save(createSamsungStock());
		StockDividend samsungStockDividend = StockDividend.create(
			1L,
			Money.won(361),
			LocalDate.of(2023, 3, 31),
			LocalDate.of(2023, 3, 30),
			LocalDate.of(2023, 5, 17),
			samsung
		);
		stockDividendRepository.save(samsungStockDividend);
	}

	@AfterEach
	void tearDown() {
		deleteDividendService.delete();
	}

	@DisplayName("원격 저장소에 배당금 데이터를 작성한다")
	@Test
	void writeDividend() throws Exception {
		// given

		// when & then
		mockMvc.perform(post("/api/dividends/write/csv")
				.cookie(createTokenCookies()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("code").value(equalTo(200)))
			.andExpect(jsonPath("status").value(equalTo("OK")))
			.andExpect(jsonPath("message").value(equalTo("배당금 데이터 작성에 성공하였습니다")))
			.andExpect(jsonPath("data").value(nullValue()));
		assertDividendFile();
	}

	private void assertDividendFile() {
		InputStream inputStream = remoteFileFetcher.read(dividendPath).orElseThrow();

		FileContentComparator.compare(inputStream, "src/test/resources/gold_dividends.csv");
	}

	@Disabled
	@DisplayName("원격 저장소에 배당금 데이터를 갱신한다")
	@Test
	void refreshStockDividend() throws Exception {
		// given
		LocalDate now = LocalDate.of(2024, 1, 1);
		LocalDate to = now.with(TemporalAdjusters.lastDayOfYear());
		BDDMockito.given(spyLocalDateTimeService.getLocalDateWithNow())
			.willReturn(now);

		KisDividend kisDividend1 = KisDividend.create(
			"005930",
			Money.won(361),
			LocalDate.of(2024, 3, 31),
			LocalDate.of(2024, 5, 20)
		);
		KisDividend kisDividend2 = KisDividend.create(
			"005930",
			Money.won(361),
			LocalDate.of(2024, 6, 30),
			LocalDate.of(2024, 8, 20)
		);
		List<KisDividend> kisDividends = Arrays.asList(kisDividend1, kisDividend2);
		BDDMockito.given(mockedKisService.fetchDividendsBetween(now, to))
			.willReturn(kisDividends);

		// when & then
		mockMvc.perform(post("/api/dividends/refresh")
				.cookie(createTokenCookies()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("code").value(equalTo(200)))
			.andExpect(jsonPath("status").value(equalTo("OK")))
			.andExpect(jsonPath("message").value(equalTo("배당 일정 최신화 완료")))
			.andExpect(jsonPath("data").value(nullValue()));
		Assertions.assertThat(stockDividendRepository.findAll()).hasSize(3);
	}

	@Disabled
	@DisplayName("배당금 데이터를 초기화한다")
	@Test
	void initializeStockDividend() throws Exception {
		// given
		StockDividend stockDividend1 = StockDividend.create(
			2L,
			Money.won(361),
			LocalDate.of(2024, 3, 31),
			LocalDate.of(2023, 3, 30),
			LocalDate.of(2023, 5, 20),
			createSamsungStock()
		);
		StockDividend stockDividend2 = StockDividend.create(
			3L,
			Money.won(361),
			LocalDate.of(2024, 6, 30),
			LocalDate.of(2023, 6, 28),
			LocalDate.of(2023, 8, 20),
			createSamsungStock()
		);
		writeDividendService.writeDividend(stockDividend1, stockDividend2);
		// when & then
		mockMvc.perform(post("/api/dividends/init")
				.cookie(createTokenCookies()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("code").value(equalTo(200)))
			.andExpect(jsonPath("status").value(equalTo("OK")))
			.andExpect(jsonPath("message").value(equalTo("배당 일정이 초기화되었습니다")))
			.andExpect(jsonPath("data").value(nullValue()));
		Assertions.assertThat(stockDividendRepository.findAll()).hasSize(2);
	}
}
