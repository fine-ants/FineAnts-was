package codesquad.fineants.global.init;

import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import codesquad.fineants.domain.stock.service.StockService;
import codesquad.fineants.domain.stock_dividend.service.StockDividendService;
import codesquad.fineants.domain.kis.service.KisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Profile(value = {"local", "dev"})
@Component
@RequiredArgsConstructor
@Slf4j
public class StartupApplicationListener implements ApplicationListener<ContextRefreshedEvent> {

	private final KisService kisService;
	private final StockService stockService;
	private final StockDividendService stockDividendService;

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {
		log.info("애플리케이션 시작시 종목 현재가 및 종가 초기화 시작");
		kisService.scheduleRefreshingAllStockCurrentPrice();
		kisService.scheduleRefreshingAllLastDayClosingPrice();
		log.info("애플리케이션 시작시 종목 현재가 및 종가 초기화 종료");
		stockService.refreshStocks();
		stockDividendService.initializeStockDividend();
	}
}