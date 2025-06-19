package co.fineants.api.domain.holding.controller;

import java.time.LocalDate;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import co.fineants.api.domain.holding.domain.dto.request.PortfolioHoldingCreateRequest;
import co.fineants.api.domain.holding.domain.dto.request.PortfolioStocksDeleteRequest;
import co.fineants.api.domain.holding.domain.dto.response.PortfolioChartResponse;
import co.fineants.api.domain.holding.domain.dto.response.PortfolioHoldingCreateResponse;
import co.fineants.api.domain.holding.domain.dto.response.PortfolioHoldingsResponse;
import co.fineants.api.domain.holding.domain.entity.PortfolioHolding;
import co.fineants.api.domain.holding.domain.factory.PortfolioStreamMessageConsumerFactory;
import co.fineants.api.domain.holding.domain.factory.PortfolioStreamerFactory;
import co.fineants.api.domain.holding.domain.factory.SseEmitterFactory;
import co.fineants.api.domain.holding.domain.factory.SseEventBuilderFactory;
import co.fineants.api.domain.holding.event.publisher.PortfolioHoldingEventPublisher;
import co.fineants.api.domain.holding.service.PortfolioHoldingFacade;
import co.fineants.api.domain.holding.service.PortfolioHoldingService;
import co.fineants.api.domain.holding.service.sender.StreamSseMessageSender;
import co.fineants.api.domain.holding.service.streamer.PortfolioStreamer;
import co.fineants.api.domain.portfolio.service.PortfolioCacheService;
import co.fineants.api.global.api.ApiResponse;
import co.fineants.api.global.security.oauth.dto.MemberAuthentication;
import co.fineants.api.global.security.oauth.resolver.MemberAuthenticationPrincipal;
import co.fineants.api.global.success.PortfolioStockSuccessCode;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequestMapping("/api/portfolio/{portfolioId}")
@RequiredArgsConstructor
@RestController
public class PortfolioHoldingRestController {

	private final PortfolioHoldingService portfolioHoldingService;
	private final PortfolioStreamerFactory marketStatusBasedPortfolioStreamerFactory;
	private final PortfolioStreamMessageConsumerFactory portfolioStreamMessageConsumerFactory;
	private final SseEmitterFactory portfolioSseEmitterFactory;
	private final SseEventBuilderFactory portfolioSseEventBuilderFactory;
	private final PortfolioCacheService portfolioCacheService;
	private final PortfolioHoldingEventPublisher publisher;
	private final PortfolioHoldingFacade portfolioHoldingFacade;

	// 포트폴리오 종목 생성
	@ResponseStatus(HttpStatus.CREATED)
	@PostMapping("/holdings")
	public ApiResponse<PortfolioHoldingCreateResponse> createPortfolioHolding(@PathVariable Long portfolioId,
		@Valid @RequestBody PortfolioHoldingCreateRequest request) {
		// 포트폴리오 종목 및 매입 이력 저장
		PortfolioHolding holding = portfolioHoldingFacade.createPortfolioHolding(request, portfolioId);
		// 포트폴리오의 종목 캐시 업데이트
		portfolioCacheService.updateTickerSymbolsFrom(portfolioId);
		// 포트폴리오 종목 추가 이벤트를 발행하여 종목 현재가 및 종가 갱신
		publisher.publishPortfolioHolding(request.getTickerSymbol());

		PortfolioHoldingCreateResponse response = PortfolioHoldingCreateResponse.from(holding);
		return ApiResponse.success(PortfolioStockSuccessCode.CREATED_ADD_PORTFOLIO_STOCK, response);
	}

	// 포트폴리오 종목 조회
	@GetMapping("/holdings")
	public ApiResponse<PortfolioHoldingsResponse> readPortfolioHoldings(@PathVariable Long portfolioId) {
		return ApiResponse.success(PortfolioStockSuccessCode.OK_READ_PORTFOLIO_STOCKS,
			portfolioHoldingService.readPortfolioHoldings(portfolioId));
	}

	// 포트폴리오 종목 실시간 조회
	@GetMapping(value = "/holdings/realtime", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
	public SseEmitter observePortfolioHoldings(@PathVariable Long portfolioId) {
		// SSE 생성
		SseEmitter emitter = portfolioSseEmitterFactory.create();
		// 현재 시간에 맞는 PortfolioStreamer 생성
		PortfolioStreamer streamer = marketStatusBasedPortfolioStreamerFactory.getStreamer();
		// Sender 생성
		StreamSseMessageSender sender = streamer.createStreamSseMessageSender(emitter,
			portfolioStreamMessageConsumerFactory);
		// 메시지 생성 및 구독
		streamer.streamMessages(portfolioId)
			.map(portfolioSseEventBuilderFactory::create)
			.subscribe(sender);
		return emitter;
	}

	// 포트폴리오 차트 조회
	@GetMapping("/charts")
	public ApiResponse<PortfolioChartResponse> readPortfolioCharts(@PathVariable Long portfolioId) {
		PortfolioChartResponse response = portfolioHoldingService.readPortfolioCharts(portfolioId, LocalDate.now());
		return ApiResponse.success(PortfolioStockSuccessCode.OK_READ_PORTFOLIO_CHARTS, response);
	}

	// 포트폴리오 종목 단일 삭제
	@DeleteMapping("/holdings/{portfolioHoldingId}")
	public ApiResponse<Void> deletePortfolioHolding(@PathVariable Long portfolioId,
		@PathVariable Long portfolioHoldingId) {
		portfolioHoldingService.deletePortfolioStock(portfolioHoldingId, portfolioId);
		return ApiResponse.success(PortfolioStockSuccessCode.OK_DELETE_PORTFOLIO_STOCK);
	}

	// 포트폴리오 종목 다수 삭제
	@DeleteMapping("/holdings")
	public ApiResponse<Void> deletePortfolioHoldings(@PathVariable Long portfolioId,
		@MemberAuthenticationPrincipal MemberAuthentication authentication,
		@Valid @RequestBody PortfolioStocksDeleteRequest request) {
		portfolioHoldingService.deletePortfolioHoldings(portfolioId, authentication.getId(),
			request.getPortfolioHoldingIds());
		return ApiResponse.success(PortfolioStockSuccessCode.OK_DELETE_PORTFOLIO_STOCKS);
	}
}
