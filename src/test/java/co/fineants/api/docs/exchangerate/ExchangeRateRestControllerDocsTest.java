package co.fineants.api.docs.exchangerate;

import static org.hamcrest.Matchers.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.*;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;

import co.fineants.api.docs.RestDocsSupport;
import co.fineants.api.domain.exchangerate.controller.ExchangeRateRestController;
import co.fineants.api.domain.exchangerate.domain.dto.response.ExchangeRateItem;
import co.fineants.api.domain.exchangerate.domain.dto.response.ExchangeRateListResponse;
import co.fineants.api.domain.exchangerate.domain.entity.ExchangeRate;
import co.fineants.api.domain.exchangerate.service.ExchangeRateService;
import co.fineants.api.domain.exchangerate.service.ExchangeRateUpdateService;
import co.fineants.api.global.success.ExchangeRateSuccessCode;
import co.fineants.api.global.util.ObjectMapperUtil;

class ExchangeRateRestControllerDocsTest extends RestDocsSupport {

	private final ExchangeRateService service = Mockito.mock(ExchangeRateService.class);
	private final ExchangeRateUpdateService exchangeRateUpdateService = Mockito.mock(ExchangeRateUpdateService.class);

	@Override
	protected Object initController() {
		return new ExchangeRateRestController(service, exchangeRateUpdateService);
	}

	@DisplayName("환율 추가 API")
	@Test
	void createExchangeRate() throws Exception {
		// given
		String usd = "USD";
		Map<String, String> code = Map.of("code", usd);

		// when & then
		mockMvc.perform(post("/api/exchange-rates")
				.contentType(MediaType.APPLICATION_JSON)
				.content(ObjectMapperUtil.serialize(code))
				.cookie(createTokenCookies()))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("code").value(equalTo(201)))
			.andExpect(jsonPath("status").value(equalTo("Created")))
			.andExpect(jsonPath("message").value(equalTo(ExchangeRateSuccessCode.CREATE_EXCHANGE_RATE.getMessage())))
			.andExpect(jsonPath("data").value(equalTo(null)))
			.andDo(
				document(
					"exchange-rate-create",
					preprocessRequest(prettyPrint()),
					preprocessResponse(prettyPrint()),
					requestFields(
						fieldWithPath("code").type(JsonFieldType.STRING)
							.description("통화 코드")
					),
					responseFields(
						fieldWithPath("code").type(JsonFieldType.NUMBER)
							.description("코드"),
						fieldWithPath("status").type(JsonFieldType.STRING)
							.description("상태"),
						fieldWithPath("message").type(JsonFieldType.STRING)
							.description("메시지"),
						fieldWithPath("data").type(JsonFieldType.NULL)
							.description("응답 데이터")
					)
				)
			);
	}

	@DisplayName("환율 목록 조회 API")
	@Test
	void readExchangeRates() throws Exception {
		// given
		ExchangeRate rate = ExchangeRate.of("USD", 0.1, false);
		BDDMockito.given(service.readExchangeRates())
			.willReturn(ExchangeRateListResponse.from(List.of(ExchangeRateItem.from(rate))));

		// when & then
		mockMvc.perform(get("/api/exchange-rates")
				.cookie(createTokenCookies()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("code").value(equalTo(200)))
			.andExpect(jsonPath("status").value(equalTo("OK")))
			.andExpect(jsonPath("message").value(equalTo(ExchangeRateSuccessCode.READ_EXCHANGE_RATE.getMessage())))
			.andExpect(jsonPath("data.rates[0].code").value(equalTo("USD")))
			.andExpect(jsonPath("data.rates[0].rate").value(equalTo(0.1)))
			.andDo(
				document(
					"exchange-rate-read",
					preprocessRequest(prettyPrint()),
					preprocessResponse(prettyPrint()),
					responseFields(
						fieldWithPath("code").type(JsonFieldType.NUMBER)
							.description("코드"),
						fieldWithPath("status").type(JsonFieldType.STRING)
							.description("상태"),
						fieldWithPath("message").type(JsonFieldType.STRING)
							.description("메시지"),
						fieldWithPath("data").type(JsonFieldType.OBJECT)
							.description("응답 데이터"),
						fieldWithPath("data.rates").type(JsonFieldType.ARRAY)
							.description("환율 데이터 리스트"),
						fieldWithPath("data.rates[].code").type(JsonFieldType.STRING)
							.description("코드"),
						fieldWithPath("data.rates[].rate").type(JsonFieldType.NUMBER)
							.description("환율")
					)
				)
			);
	}

	@DisplayName("환율 업데이트 API")
	@Test
	void updateExchangeRates() throws Exception {
		// given

		// when & then
		mockMvc.perform(put("/api/exchange-rates")
				.cookie(createTokenCookies()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("code").value(equalTo(200)))
			.andExpect(jsonPath("status").value(equalTo("OK")))
			.andExpect(jsonPath("message").value(equalTo(ExchangeRateSuccessCode.UPDATE_EXCHANGE_RATE.getMessage())))
			.andExpect(jsonPath("data").value(equalTo(null)))
			.andDo(
				document(
					"exchange-rate-update",
					preprocessRequest(prettyPrint()),
					preprocessResponse(prettyPrint()),
					responseFields(
						fieldWithPath("code").type(JsonFieldType.NUMBER)
							.description("코드"),
						fieldWithPath("status").type(JsonFieldType.STRING)
							.description("상태"),
						fieldWithPath("message").type(JsonFieldType.STRING)
							.description("메시지"),
						fieldWithPath("data").type(JsonFieldType.NULL)
							.description("응답 데이터")
					)
				)
			);
	}

	@DisplayName("환율 기준 통화 수정 API")
	@Test
	void patchBase() throws Exception {
		// given

		// when & then
		mockMvc.perform(patch("/api/exchange-rates/base")
				.queryParam("code", "KRW")
				.cookie(createTokenCookies()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("code").value(equalTo(200)))
			.andExpect(jsonPath("status").value(equalTo("OK")))
			.andExpect(jsonPath("message").value(equalTo(ExchangeRateSuccessCode.PATCH_EXCHANGE_RATE.getMessage())))
			.andExpect(jsonPath("data").value(equalTo(null)))
			.andDo(
				document(
					"exchange-rate-patch-base",
					preprocessRequest(prettyPrint()),
					preprocessResponse(prettyPrint()),
					queryParameters(
						parameterWithName("code").description("기준 통화로 변경할 통화 코드")
					),
					responseFields(
						fieldWithPath("code").type(JsonFieldType.NUMBER)
							.description("코드"),
						fieldWithPath("status").type(JsonFieldType.STRING)
							.description("상태"),
						fieldWithPath("message").type(JsonFieldType.STRING)
							.description("메시지"),
						fieldWithPath("data").type(JsonFieldType.NULL)
							.description("응답 데이터")
					)
				)
			);
	}

	@DisplayName("통화 환율 변경 API")
	@Test
	void updateRate() throws Exception {
		// given
		Map<String, Double> rates = Map.of(
			"KRW", 1.0,
			"USD", 0.2
		);
		BDDMockito.given(service.updateRate("USD", 0.2))
			.willReturn(rates);
		// when & then
		mockMvc.perform(patch("/api/exchange-rates/rate")
				.queryParam("code", "USD")
				.queryParam("newRate", "0.2")
				.cookie(createTokenCookies()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("code").value(equalTo(200)))
			.andExpect(jsonPath("status").value(equalTo("OK")))
			.andExpect(
				jsonPath("message").value(equalTo(ExchangeRateSuccessCode.PATCH_EXCHANGE_RATE_BY_CODE.getMessage())))
			.andExpect(jsonPath("data.rates").value(equalToObject(rates)))
			.andDo(
				document(
					"exchange-rate-patch-rate",
					preprocessRequest(prettyPrint()),
					preprocessResponse(prettyPrint()),
					queryParameters(
						parameterWithName("code").description("변경하고자 하는 통화 코드"),
						parameterWithName("newRate").description("변경하고자 하는 환율")
					),
					responseFields(
						fieldWithPath("code").type(JsonFieldType.NUMBER)
							.description("코드"),
						fieldWithPath("status").type(JsonFieldType.STRING)
							.description("상태"),
						fieldWithPath("message").type(JsonFieldType.STRING)
							.description("메시지"),
						fieldWithPath("data").type(JsonFieldType.OBJECT)
							.description("응답 데이터"),
						fieldWithPath("data.rates").type(JsonFieldType.OBJECT)
							.description("환율 데이터 (통화 코드 - 환율 매핑)"),
						fieldWithPath("data.rates.KRW").type(JsonFieldType.NUMBER)
							.description("KRW에 대한 환율"),
						fieldWithPath("data.rates.USD").type(JsonFieldType.NUMBER)
							.description("USD에 대한 환율")
					)
				)
			);
	}

	@DisplayName("환율 삭제 API")
	@Test
	void deleteExchangeRates() throws Exception {
		// given

		// when & then
		mockMvc.perform(delete("/api/exchange-rates")
				.contentType(MediaType.APPLICATION_JSON)
				.content(ObjectMapperUtil.serialize(Map.of("codes", List.of("USD"))))
				.cookie(createTokenCookies()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("code").value(equalTo(200)))
			.andExpect(jsonPath("status").value(equalTo("OK")))
			.andExpect(jsonPath("message").value(equalTo(ExchangeRateSuccessCode.DELETE_EXCHANGE_RATE.getMessage())))
			.andExpect(jsonPath("data").value(equalTo(null)))
			.andDo(
				document(
					"exchange-rate-delete",
					preprocessRequest(prettyPrint()),
					preprocessResponse(prettyPrint()),
					requestFields(
						fieldWithPath("codes").type(JsonFieldType.ARRAY)
							.description("통화 코드 목록")
					),
					responseFields(
						fieldWithPath("code").type(JsonFieldType.NUMBER)
							.description("코드"),
						fieldWithPath("status").type(JsonFieldType.STRING)
							.description("상태"),
						fieldWithPath("message").type(JsonFieldType.STRING)
							.description("메시지"),
						fieldWithPath("data").type(JsonFieldType.NULL)
							.description("응답 데이터")
					)
				)
			);
	}
}
