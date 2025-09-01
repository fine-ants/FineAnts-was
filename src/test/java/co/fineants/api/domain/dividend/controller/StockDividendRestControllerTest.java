package co.fineants.api.domain.dividend.controller;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.io.InputStream;
import java.time.LocalDate;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
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
import co.fineants.api.domain.stock.domain.entity.Stock;
import co.fineants.api.domain.stock.repository.StockRepository;
import co.fineants.api.global.errors.handler.GlobalExceptionHandler;
import co.fineants.api.global.security.oauth.resolver.MemberAuthenticationArgumentResolver;
import co.fineants.api.infra.s3.service.FileContentComparator;
import co.fineants.api.infra.s3.service.RemoteFileFetcher;

@WithMockUser(roles = {"ADMIN"})
class StockDividendRestControllerTest extends AbstractContainerBaseTest {

	private MockMvc mockMvc;

	@Autowired
	private GlobalExceptionHandler globalExceptionHandler;

	@Autowired
	protected MemberAuthenticationArgumentResolver mockedMemberAuthenticationArgumentResolver;

	@Autowired
	protected ObjectMapper objectMapper;

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
		// todo: delete dividends.csv
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
		InputStream inputStream = remoteFileFetcher.read(dividendPath);

		FileContentComparator comparator = new FileContentComparator();
		comparator.compare(inputStream, "src/test/resources/gold_dividends.csv");
	}
}
