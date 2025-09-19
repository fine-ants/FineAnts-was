package co.fineants.support.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.core.MethodParameter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;

import co.fineants.api.domain.common.count.Count;
import co.fineants.api.domain.common.money.Money;
import co.fineants.api.domain.dividend.domain.calculator.ExDividendDateCalculator;
import co.fineants.api.domain.dividend.domain.calculator.FileExDividendDateCalculator;
import co.fineants.api.domain.dividend.domain.entity.StockDividend;
import co.fineants.api.domain.dividend.domain.reader.HolidayFileReader;
import co.fineants.api.domain.fcm.controller.FcmRestController;
import co.fineants.api.domain.gainhistory.domain.entity.PortfolioGainHistory;
import co.fineants.api.domain.holding.controller.PortfolioHoldingRestController;
import co.fineants.api.domain.holding.domain.entity.PortfolioHolding;
import co.fineants.api.domain.kis.repository.FileHolidayRepository;
import co.fineants.api.domain.member.controller.MemberNotificationRestController;
import co.fineants.api.domain.member.controller.MemberRestController;
import co.fineants.api.domain.member.controller.SignUpRestControllerTest;
import co.fineants.api.domain.member.domain.entity.Member;
import co.fineants.api.domain.member.domain.entity.MemberProfile;
import co.fineants.api.domain.portfolio.controller.PortFolioRestController;
import co.fineants.api.domain.portfolio.controller.PortfolioNotificationRestController;
import co.fineants.api.domain.portfolio.controller.PortfolioNotificationSettingRestController;
import co.fineants.api.domain.portfolio.domain.entity.Portfolio;
import co.fineants.api.domain.portfolio.domain.entity.PortfolioDetail;
import co.fineants.api.domain.portfolio.domain.entity.PortfolioFinancial;
import co.fineants.api.domain.portfolio.properties.PortfolioProperties;
import co.fineants.api.domain.purchasehistory.controller.PurchaseHistoryRestController;
import co.fineants.api.domain.purchasehistory.domain.entity.PurchaseHistory;
import co.fineants.api.domain.stock.controller.StockRestController;
import co.fineants.api.domain.stock.domain.entity.Market;
import co.fineants.api.domain.stock.domain.entity.Stock;
import co.fineants.api.domain.stock_target_price.controller.StockTargetPriceRestController;
import co.fineants.api.domain.stock_target_price.controller.TargetPriceNotificationRestController;
import co.fineants.api.domain.watchlist.controller.WatchListRestController;
import co.fineants.api.global.config.JpaAuditingConfiguration;
import co.fineants.api.global.config.SpringConfig;
import co.fineants.api.global.config.jackson.JacksonConfig;
import co.fineants.api.global.errors.handler.GlobalExceptionHandler;
import co.fineants.api.global.security.oauth.dto.MemberAuthentication;
import co.fineants.api.global.security.oauth.resolver.MemberAuthenticationArgumentResolver;
import co.fineants.config.ControllerTestConfig;

@ActiveProfiles("test")
@Import(value = {SpringConfig.class, JacksonConfig.class, ControllerTestConfig.class})
@MockBean(JpaAuditingConfiguration.class)
@WebMvcTest(controllers = {
	MemberNotificationRestController.class,
	MemberRestController.class,
	FcmRestController.class,
	PortFolioRestController.class,
	PortfolioHoldingRestController.class,
	PortfolioNotificationRestController.class,
	PortfolioNotificationSettingRestController.class,
	PurchaseHistoryRestController.class,
	SignUpRestControllerTest.class,
	StockRestController.class,
	StockTargetPriceRestController.class,
	TargetPriceNotificationRestController.class,
	WatchListRestController.class
})
public abstract class ControllerTestSupport {

	protected MockMvc mockMvc;
	@Autowired
	protected ObjectMapper objectMapper;
	@Autowired
	protected MemberAuthenticationArgumentResolver mockedMemberAuthenticationArgumentResolver;
	@Autowired
	private GlobalExceptionHandler globalExceptionHandler;
	@Autowired
	private PortfolioProperties properties;
	private ExDividendDateCalculator exDividendDateCalculator;

	protected static Member createMember() {
		MemberProfile profile = MemberProfile.localMemberProfile("dragonbead95@naver.com", "nemo1234", "nemo1234@",
			"profileUrl");
		return Member.createMember(profile);
	}

	@BeforeEach
	void setup() throws Exception {
		mockMvc = MockMvcBuilders.standaloneSetup(initController())
			.setControllerAdvice(globalExceptionHandler)
			.setCustomArgumentResolvers(mockedMemberAuthenticationArgumentResolver)
			.setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
			.alwaysDo(print())
			.build();

		given(mockedMemberAuthenticationArgumentResolver.supportsParameter(ArgumentMatchers.any(MethodParameter.class)))
			.willReturn(true);
		given(mockedMemberAuthenticationArgumentResolver.resolveArgument(any(), any(), any(), any()))
			.willReturn(createMemberAuthentication());
		this.exDividendDateCalculator = new FileExDividendDateCalculator(
			new FileHolidayRepository(new HolidayFileReader()));
	}

	private MemberAuthentication createMemberAuthentication() {
		return MemberAuthentication.create(
			1L,
			"dragonbead95@naver.com",
			"일개미1234",
			"local",
			"profileUrl",
			Set.of("ROLE_USER")
		);
	}

	protected abstract Object initController();

	protected Portfolio createPortfolio(Member member) {
		return createPortfolio(
			member,
			Money.won(1000000)
		);
	}

	protected Portfolio createPortfolio(Member member, Money budget) {
		return createPortfolio(
			member,
			"내꿈은 워렌버핏",
			budget,
			Money.won(1500000L),
			Money.won(900000L)
		);
	}

	protected Portfolio createPortfolio(Member member, String name, Money budget, Money targetGain, Money maximumLoss) {
		return createPortfolio(1L, member, name, budget, targetGain, maximumLoss);
	}

	protected Portfolio createPortfolio(Long id, Member member, String name, Money budget, Money targetGain,
		Money maximumLoss) {
		PortfolioDetail detail = PortfolioDetail.of(name, "토스증권", properties);
		PortfolioFinancial financial = PortfolioFinancial.of(budget, targetGain, maximumLoss);
		return Portfolio.allActive(id, detail, financial, member);
	}

	protected PortfolioHolding createPortfolioHolding(Portfolio portfolio, Stock stock) {
		return PortfolioHolding.of(1L, portfolio, stock);
	}

	protected Stock createSamsungStock() {
		return Stock.of("005930", "삼성전자보통주", "SamsungElectronics", "KR7005930003", "전기전자", Market.KOSPI);
	}

	protected StockDividend createStockDividend(LocalDate recordDate, LocalDate paymentDate,
		Stock stock) {
		LocalDate exDividendDate = exDividendDateCalculator.calculate(recordDate);
		return StockDividend.create(Money.won(361), recordDate, exDividendDate, paymentDate, stock);
	}

	protected PurchaseHistory createPurchaseHistory(Long id, LocalDateTime purchaseDate, Count count,
		Money purchasePricePerShare, String memo, PortfolioHolding portfolioHolding) {
		return PurchaseHistory.create(id, purchaseDate, count, purchasePricePerShare, memo, portfolioHolding);
	}

	protected PortfolioGainHistory createEmptyPortfolioGainHistory(Portfolio portfolio) {
		return PortfolioGainHistory.empty(portfolio);
	}
}
