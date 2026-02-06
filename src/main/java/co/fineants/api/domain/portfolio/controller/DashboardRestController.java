package co.fineants.api.domain.portfolio.controller;

import java.util.List;
import java.util.Set;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import co.fineants.api.domain.portfolio.domain.dto.response.DashboardLineChartResponse;
import co.fineants.api.domain.portfolio.domain.dto.response.DashboardPieChartResponse;
import co.fineants.api.domain.portfolio.domain.dto.response.OverviewResponse;
import co.fineants.api.domain.portfolio.service.DashboardService;
import co.fineants.api.domain.portfolio.service.PortfolioService;
import co.fineants.api.global.api.ApiResponse;
import co.fineants.api.global.security.oauth.dto.MemberAuthentication;
import co.fineants.api.global.security.oauth.resolver.MemberAuthenticationPrincipal;
import co.fineants.api.global.success.DashboardSuccessCode;
import co.fineants.stock.event.StocksViewedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@RestController
public class DashboardRestController {
	private final DashboardService dashboardService;
	private final PortfolioService portfolioService;
	private final ApplicationEventPublisher eventPublisher;

	@GetMapping("/overview")
	public ApiResponse<OverviewResponse> readOverview(
		@MemberAuthenticationPrincipal MemberAuthentication authentication) {
		// 활성 종목 등록
		Set<String> tickers = portfolioService.getAllPortfolioTickers(authentication.getId());
		eventPublisher.publishEvent(new StocksViewedEvent(tickers));

		return ApiResponse.success(DashboardSuccessCode.OK_OVERVIEW,
			dashboardService.getOverview(authentication.getId()));
	}

	@GetMapping("/pieChart")
	public ApiResponse<List<DashboardPieChartResponse>> readPieChart(
		@MemberAuthenticationPrincipal MemberAuthentication authentication) {
		// 활성 종목 등록
		Set<String> tickers = portfolioService.getAllPortfolioTickers(authentication.getId());
		eventPublisher.publishEvent(new StocksViewedEvent(tickers));
		
		return ApiResponse.success(DashboardSuccessCode.OK_PORTFOLIO_PIE_CHART,
			dashboardService.getPieChart(authentication.getId()));
	}

	@GetMapping("/lineChart")
	public ApiResponse<List<DashboardLineChartResponse>> readLineChart(
		@MemberAuthenticationPrincipal MemberAuthentication authentication) {
		List<DashboardLineChartResponse> response = dashboardService.getLineChart(authentication.getId());
		log.debug("response = {}", response);
		return ApiResponse.success(DashboardSuccessCode.OK_LINE_CHART,
			dashboardService.getLineChart(authentication.getId()));
	}
}
