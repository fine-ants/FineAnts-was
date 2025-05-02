package co.fineants.api.docs.portfolio;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.anyLong;
import static org.mockito.BDDMockito.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.*;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.restdocs.payload.JsonFieldType;

import co.fineants.api.docs.RestDocsSupport;
import co.fineants.api.domain.common.count.Count;
import co.fineants.api.domain.common.money.Bank;
import co.fineants.api.domain.common.money.Currency;
import co.fineants.api.domain.common.money.Expression;
import co.fineants.api.domain.common.money.Money;
import co.fineants.api.domain.common.money.Percentage;
import co.fineants.api.domain.common.money.RateDivision;
import co.fineants.api.domain.member.domain.entity.Member;
import co.fineants.api.domain.portfolio.controller.PortFolioRestController;
import co.fineants.api.domain.portfolio.domain.dto.request.PortfolioCreateRequest;
import co.fineants.api.domain.portfolio.domain.dto.request.PortfolioModifyRequest;
import co.fineants.api.domain.portfolio.domain.dto.response.PortFolioCreateResponse;
import co.fineants.api.domain.portfolio.domain.dto.response.PortFolioItem;
import co.fineants.api.domain.portfolio.domain.dto.response.PortfolioModifyResponse;
import co.fineants.api.domain.portfolio.domain.dto.response.PortfoliosResponse;
import co.fineants.api.domain.portfolio.domain.entity.Portfolio;
import co.fineants.api.domain.portfolio.service.PortFolioService;
import co.fineants.api.global.common.page.CustomPageRequest;
import co.fineants.api.global.success.PortfolioSuccessCode;
import co.fineants.api.global.util.ObjectMapperUtil;

class PortfolioRestControllerDocsTest extends RestDocsSupport {

	private final PortFolioService portFolioService = Mockito.mock(PortFolioService.class);

	@Override
	protected Object initController() {
		return new PortFolioRestController(portFolioService);
	}

	@DisplayName("포트폴리오 생성 API")
	@Test
	void createPortfolio() throws Exception {
		// given
		Member member = createMember();
		PortfolioCreateRequest request = PortfolioCreateRequest.create(
			"내꿈은 워렌버핏",
			"토스",
			Money.won(1000000),
			Money.won(1500000L),
			Money.won(900000L)
		);
		given(portFolioService.createPortfolio(any(PortfolioCreateRequest.class), anyLong()))
			.willReturn(PortFolioCreateResponse.from(createPortfolio(member)));

		// when & then
		mockMvc.perform(RestDocumentationRequestBuilders.post("/api/portfolios")
				.cookie(createTokenCookies())
				.contentType(MediaType.APPLICATION_JSON)
				.content(ObjectMapperUtil.serialize(request)))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("code").value(equalTo(HttpStatus.CREATED.value())))
			.andExpect(jsonPath("status").value(equalTo(HttpStatus.CREATED.getReasonPhrase())))
			.andExpect(jsonPath("message").value(equalTo(PortfolioSuccessCode.CREATED_ADD_PORTFOLIO.getMessage())))
			.andExpect(jsonPath("data.portfolioId").value(equalTo(1)))
			.andDo(
				document("portfolio-create",
					preprocessRequest(prettyPrint()),
					preprocessResponse(prettyPrint()),
					requestFields(
						fieldWithPath("name").type(JsonFieldType.STRING).description("이름"),
						fieldWithPath("securitiesFirm").type(JsonFieldType.STRING).description("증권사"),
						fieldWithPath("budget").type(JsonFieldType.NUMBER).description("예산"),
						fieldWithPath("targetGain").type(JsonFieldType.NUMBER).description("목표 수익 금액"),
						fieldWithPath("maximumLoss").type(JsonFieldType.NUMBER).description("최대 손실 금액")
					),
					responseFields(
						fieldWithPath("code").type(JsonFieldType.NUMBER).description("코드"),
						fieldWithPath("status").type(JsonFieldType.STRING).description("상태"),
						fieldWithPath("message").type(JsonFieldType.STRING).description("메시지"),
						fieldWithPath("data").type(JsonFieldType.OBJECT).description("응답 데이터"),
						fieldWithPath("data.portfolioId").type(JsonFieldType.NUMBER).description("포트폴리오 등록번호")
					)
				)
			);
	}

	@DisplayName("포트폴리오 목록 조회 API")
	@Test
	void searchMyAllPortfolios() throws Exception {
		// given
		Expression totalGain = Money.won(100000);
		Expression totalInvestmentAmount = Money.won(1000000);
		Percentage totalGainRate = RateDivision.of(totalGain, totalInvestmentAmount)
			.toPercentage(Bank.getInstance(), Currency.KRW);

		Expression dailyGain = Money.won(100000L);
		Percentage dailyGainRate = RateDivision.of(dailyGain, totalInvestmentAmount)
			.toPercentage(Bank.getInstance(), Currency.KRW);

		Bank bank = Bank.getInstance();
		Currency to = Currency.KRW;
		PortFolioItem portFolioItem = PortFolioItem.builder()
			.id(1L)
			.securitiesFirm("토스증권")
			.name("내꿈은 워렌버핏")
			.budget(Money.won(1000000L))
			.totalGain(totalGain.reduce(bank, to))
			.totalGainRate(totalGainRate)
			.dailyGain(dailyGain.reduce(bank, to))
			.dailyGainRate(dailyGainRate)
			.currentValuation(Money.won(100000L))
			.expectedMonthlyDividend(Money.won(20000L))
			.numShares(Count.from(9))
			.dateCreated(LocalDateTime.now())
			.build();
		given(portFolioService.readMyAllPortfolio(anyLong()))
			.willReturn(PortfoliosResponse.builder()
				.portfolios(List.of(portFolioItem))
				.build());

		// when & then
		mockMvc.perform(get("/api/portfolios")
				.cookie(createTokenCookies()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("code").value(equalTo(200)))
			.andExpect(jsonPath("status").value(equalTo("OK")))
			.andExpect(jsonPath("message").value(equalTo("포트폴리오 목록 조회가 완료되었습니다")))
			.andExpect(jsonPath("data.portfolios[0].id").value(equalTo(portFolioItem.getId().intValue())))
			.andExpect(jsonPath("data.portfolios[0].securitiesFirm").value(equalTo("토스증권")))
			.andExpect(jsonPath("data.portfolios[0].name").value(equalTo("내꿈은 워렌버핏")))
			.andExpect(jsonPath("data.portfolios[0].budget").value(equalTo(1000000)))
			.andExpect(jsonPath("data.portfolios[0].totalGain").value(equalTo(100000)))
			.andExpect(jsonPath("data.portfolios[0].totalGainRate").value(equalTo(10.0)))
			.andExpect(jsonPath("data.portfolios[0].dailyGain").value(equalTo(100000)))
			.andExpect(jsonPath("data.portfolios[0].dailyGainRate").value(equalTo(10.0)))
			.andExpect(jsonPath("data.portfolios[0].currentValuation")
				.value(equalTo(100000)))
			.andExpect(jsonPath("data.portfolios[0].expectedMonthlyDividend")
				.value(equalTo(20000)))
			.andExpect(jsonPath("data.portfolios[0].numShares")
				.value(equalTo(9)))
			.andExpect(jsonPath("data.portfolios[0].dateCreated").isNotEmpty())
			.andDo(
				document(
					"portfolio-search",
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
						fieldWithPath("data.portfolios[].id").type(JsonFieldType.NUMBER)
							.description("포트폴리오 등록번호"),
						fieldWithPath("data.portfolios[].securitiesFirm").type(JsonFieldType.STRING)
							.description("증권사"),
						fieldWithPath("data.portfolios[].name").type(JsonFieldType.STRING)
							.description("포트폴리오 이름"),
						fieldWithPath("data.portfolios[].budget").type(JsonFieldType.NUMBER)
							.description("예산"),
						fieldWithPath("data.portfolios[].totalGain").type(JsonFieldType.NUMBER)
							.description("총손익"),
						fieldWithPath("data.portfolios[].totalGainRate").type(JsonFieldType.NUMBER)
							.description("총손익율"),
						fieldWithPath("data.portfolios[].dailyGain").type(JsonFieldType.NUMBER)
							.description("일일손익"),
						fieldWithPath("data.portfolios[].dailyGainRate").type(JsonFieldType.NUMBER)
							.description("일일손익율"),
						fieldWithPath("data.portfolios[].currentValuation").type(JsonFieldType.NUMBER)
							.description("현재평가금액"),
						fieldWithPath("data.portfolios[].expectedMonthlyDividend").type(JsonFieldType.NUMBER)
							.description("예상월배당금"),
						fieldWithPath("data.portfolios[].numShares").type(JsonFieldType.NUMBER)
							.description("주식 총개수"),
						fieldWithPath("data.portfolios[].dateCreated").type(JsonFieldType.STRING)
							.description("추가일자")
					)
				)
			);
	}

	@DisplayName("포트폴리오 이름 목록 API")
	@Test
	void searchMyAllPortfolioNames() throws Exception {
		// given
		Member member = createMember();
		Portfolio portfolio = createPortfolio(member);
		Pageable pageable = new CustomPageRequest(0, 10, Sort.Direction.DESC).of();
		given(portFolioService.readMyAllPortfolioNamesUsingPaging(member.getId(), pageable))
			.willReturn(new PageImpl<>(List.of(portfolio), pageable, 1));

		// when & then
		mockMvc.perform(get("/api/portfolios/names")
				.cookie(createTokenCookies()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("code").value(equalTo(200)))
			.andExpect(jsonPath("status").value(equalTo("OK")))
			.andExpect(jsonPath("message").value(equalTo(PortfolioSuccessCode.OK_SEARCH_PORTFOLIO_NAMES.getMessage())))
			.andExpect(jsonPath("data.pageable.pageNumber").value(equalTo(0)))
			.andExpect(jsonPath("data.pageable.pageSize").value(equalTo(10)))
			.andExpect(jsonPath("data.pageable.sort.empty").value(equalTo(false)))
			.andExpect(jsonPath("data.pageable.sort.unsorted").value(equalTo(false)))
			.andExpect(jsonPath("data.pageable.sort.sorted").value(equalTo(true)))
			.andExpect(jsonPath("data.pageable.offset").value(equalTo(0)))
			.andExpect(jsonPath("data.pageable.paged").value(equalTo(true)))
			.andExpect(jsonPath("data.pageable.unpaged").value(equalTo(false)))
			.andExpect(jsonPath("data.first").value(equalTo(true)))
			.andExpect(jsonPath("data.last").value(equalTo(true)))
			.andExpect(jsonPath("data.totalElements").value(equalTo(1)))
			.andExpect(jsonPath("data.totalPages").value(equalTo(1)))
			.andExpect(jsonPath("data.size").value(equalTo(10)))
			.andExpect(jsonPath("data.number").value(equalTo(0)))
			.andExpect(jsonPath("data.sort.empty").value(equalTo(false)))
			.andExpect(jsonPath("data.sort.unsorted").value(equalTo(false)))
			.andExpect(jsonPath("data.sort.sorted").value(equalTo(true)))
			.andExpect(jsonPath("data.numberOfElements").value(equalTo(1)))
			.andExpect(jsonPath("data.empty").value(equalTo(false)))
			.andExpect(jsonPath("data.portfolios").isArray())
			.andExpect(jsonPath("data.portfolios[0].id").value(equalTo(portfolio.getId().intValue())))
			.andExpect(jsonPath("data.portfolios[0].name").value(equalTo("내꿈은 워렌버핏")))
			.andExpect(jsonPath("data.portfolios[0].dateCreated").isNotEmpty())
			.andDo(
				document(
					"portfolio-search-names",
					preprocessRequest(prettyPrint()),
					preprocessResponse(prettyPrint()),
					queryParameters(
						parameterWithName("page").description("페이지 번호(0부터 시작, 기본값: 0)").optional(),
						parameterWithName("size").description("페이지 크기(한 페이지에 포함된 항목 개수, 기본값: 10)").optional(),
						parameterWithName("direction").description("정렬 방향(ASC 또는 DESC, 기본값: DESC)").optional()
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
						fieldWithPath("data.pageable.pageNumber").type(JsonFieldType.NUMBER)
							.description("현재 페이지 번호(0부터 시작)"),
						fieldWithPath("data.pageable.pageSize").type(JsonFieldType.NUMBER)
							.description("한 페이지에 포함된 항목 개수"),
						fieldWithPath("data.pageable.sort").type(JsonFieldType.OBJECT)
							.description("정렬 정보"),
						fieldWithPath("data.pageable.sort.empty").type(JsonFieldType.BOOLEAN)
							.description("정렬 기준이 비어 있는지 여부(false: 정렬 기준 존재, true: 정렬 기준 없음)"),
						fieldWithPath("data.pageable.sort.unsorted").type(JsonFieldType.BOOLEAN)
							.description("정렬이 적용되지 않았는지 여부(false: 정렬 적용, true: 정렬 미적용)"),
						fieldWithPath("data.pageable.sort.sorted").type(JsonFieldType.BOOLEAN)
							.description("정렬이 적용되었는지 여부(false: 정렬 미적용, true: 정렬 적용)"),
						fieldWithPath("data.pageable.offset").type(JsonFieldType.NUMBER)
							.description("전체 결과에서 현재 페이지의 시작 위(예: 0페이이면 0, 1페이지 pageSize*1"),
						fieldWithPath("data.pageable.paged").type(JsonFieldType.BOOLEAN)
							.description("페이징이 적용되었는지 여부"),
						fieldWithPath("data.pageable.unpaged").type(JsonFieldType.BOOLEAN)
							.description("페이징이 적용되지 않았는지 여부"),
						fieldWithPath("data.first").type(JsonFieldType.BOOLEAN)
							.description("첫 페이지 여부"),
						fieldWithPath("data.last").type(JsonFieldType.BOOLEAN)
							.description("마지막 페이지 여부"),
						fieldWithPath("data.totalElements").type(JsonFieldType.NUMBER)
							.description("전체 항목 개수"),
						fieldWithPath("data.totalPages").type(JsonFieldType.NUMBER)
							.description("전체 페이지 개수"),
						fieldWithPath("data.size").type(JsonFieldType.NUMBER)
							.description("한 페이지에 포함된 항목 개수"),
						fieldWithPath("data.number").type(JsonFieldType.NUMBER)
							.description("현재 페이지 번호(0부터 시작)"),
						fieldWithPath("data.sort").type(JsonFieldType.OBJECT)
							.description("정렬 정보"),
						fieldWithPath("data.sort.empty").type(JsonFieldType.BOOLEAN)
							.description("정렬 기준이 비어 있는지 여부(false: 정렬 기준 존재, true: 정렬 기준 없음)"),
						fieldWithPath("data.sort.unsorted").type(JsonFieldType.BOOLEAN)
							.description("정렬이 적용되지 않았는지 여부(false: 정렬 적용, true: 정렬 미적용)"),
						fieldWithPath("data.sort.sorted").type(JsonFieldType.BOOLEAN)
							.description("정렬이 적용되었는지 여부(false: 정렬 미적용, true: 정렬 적용)"),
						fieldWithPath("data.numberOfElements").type(JsonFieldType.NUMBER)
							.description("현재 페이지에 포함된 항목 개수"),
						fieldWithPath("data.empty").type(JsonFieldType.BOOLEAN)
							.description("현재 페이지가 비어 있는지 여부"),
						fieldWithPath("data.portfolios").type(JsonFieldType.ARRAY)
							.description("포트폴리오 목록"),
						fieldWithPath("data.portfolios[].id").type(JsonFieldType.NUMBER)
							.description("포트폴리오 등록번호"),
						fieldWithPath("data.portfolios[].name").type(JsonFieldType.STRING)
							.description("포트폴리오 이름"),
						fieldWithPath("data.portfolios[].dateCreated").type(JsonFieldType.STRING)
							.description("생성일자")
					)
				)
			);
	}

	@DisplayName("포트폴리오 수정 API")
	@Test
	void updatePortfolio() throws Exception {
		// given
		Member member = createMember();
		Portfolio portfolio = createPortfolio(member);
		given(portFolioService.updatePortfolio(
			any(PortfolioModifyRequest.class),
			anyLong(),
			anyLong()))
			.willReturn(PortfolioModifyResponse.from(portfolio));

		Map<String, Object> body = new HashMap<>();
		body.put("name", "내꿈은 워렌버핏");
		body.put("securitiesFirm", "토스");
		body.put("budget", 1000000L);
		body.put("targetGain", 1500000L);
		body.put("maximumLoss", 900000L);

		// when & then
		mockMvc.perform(RestDocumentationRequestBuilders.put("/api/portfolios/{portfolioId}", portfolio.getId())
				.cookie(createTokenCookies())
				.contentType(MediaType.APPLICATION_JSON)
				.content(ObjectMapperUtil.serialize(body)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("code").value(equalTo(200)))
			.andExpect(jsonPath("status").value(equalTo("OK")))
			.andExpect(jsonPath("message").value(equalTo("포트폴리오가 수정되었습니다")))
			.andExpect(jsonPath("data").value(equalTo(null)))
			.andDo(
				document(
					"portfolio-update",
					preprocessRequest(prettyPrint()),
					preprocessResponse(prettyPrint()),
					pathParameters(
						parameterWithName("portfolioId").description("포트폴리오 등록번호")
					),
					requestFields(
						fieldWithPath("name").type(JsonFieldType.STRING).description("이름"),
						fieldWithPath("securitiesFirm").type(JsonFieldType.STRING).description("증권사"),
						fieldWithPath("budget").type(JsonFieldType.NUMBER).description("예산"),
						fieldWithPath("targetGain").type(JsonFieldType.NUMBER).description("목표 수익 금액"),
						fieldWithPath("maximumLoss").type(JsonFieldType.NUMBER).description("최대 손실 금액")
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

	@DisplayName("포트폴리오 단일 삭제 API")
	@Test
	void deletePortfolio() throws Exception {
		// given
		Member member = createMember();
		Portfolio portfolio = createPortfolio(member);

		// when & then
		mockMvc.perform(RestDocumentationRequestBuilders.delete("/api/portfolios/{portfolioId}", portfolio.getId())
				.cookie(createTokenCookies()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("code").value(equalTo(200)))
			.andExpect(jsonPath("status").value(equalTo("OK")))
			.andExpect(jsonPath("message").value(equalTo("포트폴리오 삭제가 완료되었습니다")))
			.andExpect(jsonPath("data").value(equalTo(null)))
			.andDo(
				document(
					"portfolio-one-delete",
					preprocessRequest(prettyPrint()),
					preprocessResponse(prettyPrint()),
					pathParameters(
						parameterWithName("portfolioId").description("포트폴리오 등록번호")
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

	@DisplayName("포트폴리오 다수 삭제 API")
	@Test
	void deletePortfolios() throws Exception {
		// given
		Member member = createMember();
		Portfolio portfolio = createPortfolio(member);

		Map<String, Object> body = Map.of(
			"portfolioIds", List.of(portfolio.getId())
		);
		// when & then
		mockMvc.perform(RestDocumentationRequestBuilders.delete("/api/portfolios")
				.cookie(createTokenCookies())
				.contentType(MediaType.APPLICATION_JSON)
				.content(ObjectMapperUtil.serialize(body)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("code").value(equalTo(200)))
			.andExpect(jsonPath("status").value(equalTo("OK")))
			.andExpect(jsonPath("message").value(equalTo("포트폴리오 삭제가 완료되었습니다")))
			.andExpect(jsonPath("data").value(equalTo(null)))
			.andDo(
				document(
					"portfolio-multiple-delete",
					preprocessRequest(prettyPrint()),
					preprocessResponse(prettyPrint()),
					requestFields(
						fieldWithPath("portfolioIds").type(JsonFieldType.ARRAY).description("포트폴리오 등록번호 리스트")
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
