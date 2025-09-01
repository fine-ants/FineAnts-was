package co.fineants.api.domain.dividend.controller;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;

import co.fineants.AbstractContainerBaseTest;
import co.fineants.api.global.errors.handler.GlobalExceptionHandler;
import co.fineants.api.global.security.oauth.resolver.MemberAuthenticationArgumentResolver;

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

	@BeforeEach
	void setUp() {
		mockMvc = MockMvcBuilders.standaloneSetup(controller)
			.setControllerAdvice(globalExceptionHandler)
			.setCustomArgumentResolvers(mockedMemberAuthenticationArgumentResolver)
			.setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
			.alwaysDo(print())
			.build();
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
		// todo: csv 파일 존재 여부 확인

		// todo: csv 파일 내용 확인
	}

}
