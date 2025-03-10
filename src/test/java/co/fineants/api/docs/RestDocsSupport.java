package co.fineants.api.docs;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.core.MethodParameter;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.ResponseCookie;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.multipart.MultipartFile;

import co.fineants.api.domain.common.count.Count;
import co.fineants.api.domain.common.money.Money;
import co.fineants.api.domain.common.notification.PortfolioTargetGainNotifiable;
import co.fineants.api.domain.common.notification.TargetPriceNotificationNotifiable;
import co.fineants.api.domain.dividend.domain.calculator.ExDividendDateCalculator;
import co.fineants.api.domain.dividend.domain.calculator.FileExDividendDateCalculator;
import co.fineants.api.domain.dividend.domain.entity.StockDividend;
import co.fineants.api.domain.dividend.domain.reader.HolidayFileReader;
import co.fineants.api.domain.gainhistory.domain.entity.PortfolioGainHistory;
import co.fineants.api.domain.holding.domain.entity.PortfolioHolding;
import co.fineants.api.domain.kis.repository.FileHolidayRepository;
import co.fineants.api.domain.member.domain.entity.Member;
import co.fineants.api.domain.member.domain.entity.MemberProfile;
import co.fineants.api.domain.notification.domain.dto.response.NotifyMessage;
import co.fineants.api.domain.notification.domain.dto.response.PortfolioNotifyMessage;
import co.fineants.api.domain.notification.domain.dto.response.StockNotifyMessage;
import co.fineants.api.domain.notification.domain.entity.Notification;
import co.fineants.api.domain.notification.domain.entity.PortfolioNotification;
import co.fineants.api.domain.portfolio.domain.entity.Portfolio;
import co.fineants.api.domain.portfolio.domain.entity.PortfolioDetail;
import co.fineants.api.domain.portfolio.domain.entity.PortfolioFinancial;
import co.fineants.api.domain.portfolio.properties.PortfolioProperties;
import co.fineants.api.domain.purchasehistory.domain.entity.PurchaseHistory;
import co.fineants.api.domain.stock.domain.entity.Market;
import co.fineants.api.domain.stock.domain.entity.Stock;
import co.fineants.api.domain.stock_target_price.domain.entity.StockTargetPrice;
import co.fineants.api.domain.stock_target_price.domain.entity.TargetPriceNotification;
import co.fineants.api.domain.watchlist.domain.entity.WatchList;
import co.fineants.api.global.config.jackson.JacksonConfig;
import co.fineants.api.global.security.factory.LocalCookieDomainProvider;
import co.fineants.api.global.security.factory.TokenFactory;
import co.fineants.api.global.security.oauth.dto.MemberAuthentication;
import co.fineants.api.global.security.oauth.dto.Token;
import co.fineants.api.global.security.oauth.resolver.MemberAuthenticationArgumentResolver;
import jakarta.servlet.http.Cookie;

@ExtendWith(RestDocumentationExtension.class)
public abstract class RestDocsSupport {
	protected MockMvc mockMvc;

	protected MemberAuthenticationArgumentResolver memberAuthenticationArgumentResolver;

	private final PortfolioProperties properties = new PortfolioProperties(new String[] {"토스증권", "FineAnts"});

	private ExDividendDateCalculator exDividendDateCalculator;

	@BeforeEach
	void setUp(RestDocumentationContextProvider provider) throws Exception {
		this.memberAuthenticationArgumentResolver = Mockito.mock(MemberAuthenticationArgumentResolver.class);
		this.mockMvc = MockMvcBuilders.standaloneSetup(initController())
			.apply(MockMvcRestDocumentation.documentationConfiguration(provider))
			.setCustomArgumentResolvers(memberAuthenticationArgumentResolver)
			.setMessageConverters(new MappingJackson2HttpMessageConverter(new JacksonConfig().objectMapper()))
			.alwaysDo(print())
			.build();

		given(memberAuthenticationArgumentResolver.supportsParameter(ArgumentMatchers.any(MethodParameter.class)))
			.willReturn(true);
		given(memberAuthenticationArgumentResolver.resolveArgument(any(), any(), any(), any()))
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

	protected Member createMember() {
		MemberProfile profile = MemberProfile.localMemberProfile("kim1234@gmail.com", "일개미1234", "kim1234@",
			"profileUrl");
		return Member.localMember(1L, profile);
	}

	protected Stock createSamsungStock() {
		return Stock.of("005930", "삼성전자보통주", "SamsungElectronics", "KR7005930003", "전기전자", Market.KOSPI);
	}

	protected Portfolio createPortfolio(Member member) {
		String name = "내꿈은 워렌버핏";
		String securitiesFirm = "토스증권";
		PortfolioDetail detail = PortfolioDetail.of(name, securitiesFirm, properties);

		Money budget = Money.won(1000000L);
		Money targetGain = Money.won(1500000L);
		Money maximumLoss = Money.won(900000L);
		PortfolioFinancial financial = PortfolioFinancial.of(budget, targetGain, maximumLoss);

		return Portfolio.allActive(1L, detail, financial, member);
	}

	protected PortfolioHolding createPortfolioHolding(Portfolio portfolio, Stock stock) {
		return PortfolioHolding.of(1L, portfolio, stock);
	}

	protected PurchaseHistory createPurchaseHistory(PortfolioHolding portfolioHolding, LocalDateTime purchaseDate) {
		return PurchaseHistory.create(1L, purchaseDate, Count.from(3L), Money.won(50000), "첫구매", portfolioHolding);
	}

	protected PortfolioGainHistory createEmptyPortfolioGainHistory(Portfolio portfolio) {
		return PortfolioGainHistory.empty(portfolio);
	}

	protected List<StockDividend> createStockDividendWith(Stock stock) {
		return List.of(
			createStockDividend(
				LocalDate.of(2022, 3, 31),
				LocalDate.of(2022, 5, 17),
				stock
			),
			createStockDividend(
				LocalDate.of(2022, 6, 30),
				LocalDate.of(2022, 8, 16),
				stock
			),
			createStockDividend(
				LocalDate.of(2022, 9, 30),
				LocalDate.of(2022, 11, 15),
				stock
			),
			createStockDividend(
				LocalDate.of(2022, 12, 31),
				LocalDate.of(2023, 4, 14),
				stock),
			createStockDividend(
				LocalDate.of(2023, 3, 31),
				LocalDate.of(2023, 5, 17),
				stock),
			createStockDividend(
				LocalDate.of(2023, 6, 30),
				LocalDate.of(2023, 8, 16),
				stock),
			createStockDividend(
				LocalDate.of(2023, 9, 30),
				LocalDate.of(2023, 11, 20),
				stock),
			createStockDividend(
				LocalDate.of(2024, 3, 31),
				LocalDate.of(2024, 5, 17),
				stock)
		);
	}

	protected StockDividend createStockDividend(LocalDate recordDate, LocalDate paymentDate,
		Stock stock) {
		LocalDate exDividendDate = exDividendDateCalculator.calculate(recordDate);
		return StockDividend.create(Money.won(361), recordDate, exDividendDate, paymentDate, stock);
	}

	protected Notification createPortfolioNotification(PortfolioNotifyMessage message, Portfolio portfolio,
		Member member) {
		return Notification.portfolioNotification(
				message.getTitle(), message.getType(), message.getReferenceId(), message.getLink(), member,
				message.getMessageIds(), portfolio.name(),
				portfolio.getId()
			)
			.withId(1L);
	}

	protected Notification createStockNotification(StockNotifyMessage message, Member member) {
		return Notification.stockTargetPriceNotification(
			message.getTitle(), message.getReferenceId(), message.getLink(), member, message.getMessageIds(),
			message.getStockName(),
			message.getTargetPrice(),
			message.getTargetPriceNotificationId()
		).withId(1L);
	}

	protected StockTargetPrice createStockTargetPrice(Member member, Stock stock) {
		return StockTargetPrice.newStockTargetPriceWithActive(1L, member, stock);
	}

	protected TargetPriceNotification createTargetPriceNotification(StockTargetPrice stockTargetPrice) {
		return TargetPriceNotification.newTargetPriceNotification(1L, Money.won(60000), stockTargetPrice);
	}

	protected PortfolioNotification createPortfolioTargetGainNotification(Portfolio portfolio, Member member) {
		NotifyMessage message = PortfolioTargetGainNotifiable.from(portfolio, true).createMessage("token");
		return (PortfolioNotification)Notification.portfolioNotification(
				message.getTitle(), message.getType(), message.getReferenceId(), message.getLink(), member,
				message.getMessageIds(), portfolio.name(),
				portfolio.getId()
			)
			.withId(1L);
	}

	protected Notification createStockTargetPriceNotification(
		TargetPriceNotification targetPriceNotification, Member member) {
		StockNotifyMessage message = (StockNotifyMessage)TargetPriceNotificationNotifiable.from(targetPriceNotification,
				true)
			.createMessage("token");
		return Notification.stockTargetPriceNotification(
			message.getTitle(),
			message.getReferenceId(),
			message.getLink(),
			member,
			message.getMessageIds(),
			message.getStockName(),
			message.getTargetPrice(),
			message.getTargetPriceNotificationId()
		).withId(1L);
	}

	protected WatchList createWatchList(Member member) {
		return WatchList.newWatchList(1L, "my watchlist 1", member);
	}

	protected MultipartFile createMockMultipartFile() {
		ClassPathResource classPathResource = new ClassPathResource("profile.jpeg");
		try {
			Path path = Paths.get(classPathResource.getURI());
			byte[] profile = Files.readAllBytes(path);
			return new MockMultipartFile("profileImageFile", "profile.jpeg", "image/jpeg",
				profile);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	protected Cookie[] createTokenCookies() {
		TokenFactory tokenFactory = new TokenFactory(new LocalCookieDomainProvider());
		Token token = Token.create("accessToken", "refreshToken");
		ResponseCookie accessTokenCookie = tokenFactory.createAccessTokenCookie(token);
		ResponseCookie refreshTokenCookie = tokenFactory.createRefreshTokenCookie(token);
		return new Cookie[] {convertCookie(accessTokenCookie), convertCookie(refreshTokenCookie)};
	}

	private Cookie convertCookie(ResponseCookie cookie) {
		String cookieString = cookie.toString();
		int start = cookieString.indexOf("=") + 1;
		return new Cookie(cookie.getName(), cookieString.substring(start));
	}

	protected abstract Object initController();
}
