package co.fineants;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.logging.log4j.util.Strings;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import co.fineants.api.domain.common.count.Count;
import co.fineants.api.domain.common.money.Money;
import co.fineants.api.domain.fcm.domain.entity.FcmToken;
import co.fineants.api.domain.holding.domain.entity.PortfolioHolding;
import co.fineants.api.domain.kis.client.KisAccessToken;
import co.fineants.api.domain.kis.repository.KisAccessTokenRepository;
import co.fineants.api.domain.portfolio.domain.entity.Portfolio;
import co.fineants.api.domain.portfolio.domain.entity.PortfolioDetail;
import co.fineants.api.domain.portfolio.domain.entity.PortfolioFinancial;
import co.fineants.api.domain.portfolio.properties.PortfolioProperties;
import co.fineants.api.domain.purchasehistory.domain.entity.PurchaseHistory;
import co.fineants.api.domain.stock.domain.entity.Market;
import co.fineants.api.domain.stock.domain.entity.Stock;
import co.fineants.api.domain.stock.domain.entity.StockDividend;
import co.fineants.api.domain.stock_target_price.domain.entity.StockTargetPrice;
import co.fineants.api.domain.stock_target_price.domain.entity.TargetPriceNotification;
import co.fineants.api.domain.watchlist.domain.entity.WatchList;
import co.fineants.api.domain.watchlist.domain.entity.WatchStock;
import co.fineants.api.global.errors.exception.business.RoleNotFoundException;
import co.fineants.api.global.security.factory.CookieDomainProvider;
import co.fineants.api.global.security.factory.TokenFactory;
import co.fineants.api.global.security.oauth.dto.MemberAuthentication;
import co.fineants.api.global.security.oauth.dto.Token;
import co.fineants.config.AmazonS3TestConfig;
import co.fineants.config.TestConfig;
import co.fineants.member.domain.Member;
import co.fineants.member.domain.MemberEmail;
import co.fineants.member.domain.MemberProfile;
import co.fineants.member.domain.Nickname;
import co.fineants.member.domain.NotificationPreference;
import co.fineants.role.domain.Role;
import co.fineants.role.domain.RoleRepository;
import co.fineants.support.mysql.DatabaseCleaner;
import co.fineants.support.redis.RedisRepository;
import jakarta.servlet.http.Cookie;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import okhttp3.mockwebserver.MockResponse;

@Slf4j
@ActiveProfiles({"test", "aws"})
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(classes = {AmazonS3TestConfig.class, TestConfig.class})
@AutoConfigureWebTestClient
@Testcontainers
@WithMockMemberAuthentication
public abstract class AbstractContainerBaseTest {
	public static final LocalStackContainer LOCAL_STACK_CONTAINER = new LocalStackContainer(
		DockerImageName.parse("localstack/localstack"))
		.withServices(LocalStackContainer.Service.S3)
		.withReuse(true);
	private static final String REDIS_IMAGE = "redis:7-alpine";
	private static final int REDIS_PORT = 6379;
	private static final GenericContainer REDIS_CONTAINER = new GenericContainer(REDIS_IMAGE)
		.withExposedPorts(REDIS_PORT)
		.withReuse(true);

	static {
		REDIS_CONTAINER.start();
		LOCAL_STACK_CONTAINER.start();
	}

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	private RoleRepository roleRepository;

	@Autowired
	private DatabaseCleaner databaseCleaner;

	@Autowired
	private KisAccessTokenRepository kisAccessTokenRepository;

	@Autowired
	private RedisRepository redisRepository;

	@Autowired
	private PortfolioProperties properties;

	@Autowired
	private CookieDomainProvider cookieDomainProvider;

	@Autowired
	private ApplicationContextInitListener applicationContextInitListener;

	@Autowired
	private ApplicationContext applicationContext;

	@DynamicPropertySource
	public static void overrideProps(DynamicPropertyRegistry registry) {
		// redis property config
		registry.add("spring.data.redis.host", REDIS_CONTAINER::getHost);
		registry.add("spring.data.redis.port", () -> REDIS_CONTAINER.getMappedPort(REDIS_PORT).toString());

		// mysql property config
		registry.add("spring.datasource.driver-class-name", () -> "org.testcontainers.jdbc.ContainerDatabaseDriver");
		registry.add("spring.datasource.url", () -> "jdbc:tc:mysql:8.0.33://localhost/fineAnts");
		registry.add("spring.datasource.username", () -> "admin");
		registry.add("spring.datasource.password", () -> "password1234!");
	}

	@NotNull
	public static MockResponse createResponse(int code, String body) {
		return new MockResponse()
			.setResponseCode(code)
			.setBody(body)
			.addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON);
	}

	@BeforeEach
	public void abstractSetup() {
		roleRepository.save(TestDataFactory.createRole("ROLE_ADMIN", "관리자"));
		roleRepository.save(TestDataFactory.createRole("ROLE_MANAGER", "매니저"));
		roleRepository.save(TestDataFactory.createRole("ROLE_USER", "회원"));
		kisAccessTokenRepository.refreshAccessToken(TestDataFactory.createKisAccessToken());
	}

	@AfterEach
	public void cleanDatabase() {
		databaseCleaner.clear();
		redisRepository.clearAll();
		System.out.println("applicationContext.hashCode() : " + applicationContext.hashCode());
		System.out.println("context init count : " + applicationContextInitListener.getContextInitCount());
	}

	public KisAccessToken createKisAccessToken() {
		return KisAccessToken.bearerType("accessToken", LocalDateTime.now().plusSeconds(86400), 86400);
	}

	protected Member createMember() {
		return createMember("nemo1234");
	}

	protected Member createMember(String nickname) {
		return createMember(nickname, "dragonbead95@naver.com");
	}

	protected Member createMember(String nicknameValue, String email) {
		String roleName = "ROLE_USER";
		Role userRole = roleRepository.findRoleByRoleName(roleName)
			.orElseThrow(() -> new RoleNotFoundException(roleName));
		// 회원 생성
		String password = passwordEncoder.encode("nemo1234@");
		MemberEmail memberEmail = new MemberEmail(email);
		Nickname nickname = new Nickname(nicknameValue);
		MemberProfile profile = MemberProfile.localMemberProfile(memberEmail, nickname, password, "profileUrl");
		NotificationPreference notificationPreference = NotificationPreference.allActive();
		Member member = Member.createMember(profile, notificationPreference);
		// 역할 설정
		member.addRoleId(userRole.getId());
		return member;
	}

	protected Member createOauthMember() {
		String roleName = "ROLE_USER";
		Role userRole = roleRepository.findRoleByRoleName(roleName)
			.orElseThrow(() -> new RoleNotFoundException(roleName));
		MemberEmail memberEmail = new MemberEmail("fineants1234@gmail.com");
		Nickname nickname = new Nickname("fineants1234");
		MemberProfile profile = MemberProfile.oauthMemberProfile(memberEmail, nickname, "google",
			"profileUrl1");
		NotificationPreference notificationPreference = NotificationPreference.allActive();
		// 회원 생성
		Member member = Member.createMember(profile, notificationPreference);
		// 역할 설정
		member.addRoleId(userRole.getId());
		return member;
	}

	protected NotificationPreference createNotificationPreference(boolean browserNotify, boolean targetGainNotify,
		boolean maxLossNotify, boolean targetPriceNotify) {
		return NotificationPreference.create(
			browserNotify,
			targetGainNotify,
			maxLossNotify,
			targetPriceNotify
		);
	}

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
		PortfolioDetail detail = PortfolioDetail.of(name, "토스증권", properties);
		PortfolioFinancial financial = PortfolioFinancial.of(budget, targetGain, maximumLoss);
		return Portfolio.allActive(
			null,
			detail,
			financial,
			member
		);
	}

	protected Portfolio createPortfolio(Member member, String name) {
		return createPortfolio(
			member,
			name,
			Money.won(1000000L),
			Money.won(1500000L),
			Money.won(900000L)
		);
	}

	protected Stock createSamsungStock() {
		return Stock.of("005930", "삼성전자보통주", "SamsungElectronics", "KR7005930003", "전기전자", Market.KOSPI);
	}

	protected Stock createDongwhaPharmStock() {
		return Stock.of("000020", "동화약품보통주", "DongwhaPharm", "KR7000020008", "의약품", Market.KOSPI);
	}

	protected Stock createCcsStack() {
		return Stock.of("066790", "씨씨에스충북방송", "KOREA CABLE T.V CHUNG-BUK SYSTEM CO.,LTD.", "KR7066790007", "방송서비스",
			Market.KOSDAQ);
	}

	/**
	 * 해당 종목은 상장 폐지된 종목입니다.
	 * @return 상장 폐지된 종목
	 */
	protected Stock createNokwonCI() {
		return Stock.of("065560", "녹원씨엔아이", "Nokwon Commercials & Industries, Inc.", "KR7065560005", "소프트웨어",
			Market.KOSDAQ);
	}

	protected Stock createKakaoStock() {
		return Stock.of("035720", "카카오보통주", "Kakao", "KR7035720002", "서비스업", Market.KOSPI);
	}

	protected PortfolioHolding createPortfolioHolding(Portfolio portfolio, Stock stock) {
		return PortfolioHolding.of(portfolio, stock);
	}

	protected PurchaseHistory createPurchaseHistory(Long id, LocalDateTime purchaseDate, Count numShares,
		Money purchasePricePerShare, String memo, PortfolioHolding portfolioHolding) {
		return PurchaseHistory.create(id, purchaseDate, numShares, purchasePricePerShare, memo, portfolioHolding);
	}

	protected FcmToken createFcmToken(String token, Member member) {
		return FcmToken.create(member, token);
	}

	protected WatchList createWatchList(Member member) {
		return createWatchList("관심 종목1", member);
	}

	protected WatchList createWatchList(String name, Member member) {
		return WatchList.newWatchList(name, member);
	}

	protected WatchStock createWatchStock(WatchList watchList, Stock stock) {
		return WatchStock.newWatchStock(watchList, stock);
	}

	protected StockTargetPrice createStockTargetPrice(Member member, Stock stock) {
		return StockTargetPrice.newStockTargetPriceWithActive(member, stock);
	}

	protected TargetPriceNotification createTargetPriceNotification(StockTargetPrice stockTargetPrice) {
		return TargetPriceNotification.newTargetPriceNotification(Money.won(60000L), stockTargetPrice);
	}

	protected List<TargetPriceNotification> createTargetPriceNotification(StockTargetPrice stockTargetPrice,
		List<Long> targetPrices) {
		return targetPrices.stream()
			.map(targetPrice -> TargetPriceNotification.newTargetPriceNotification(Money.won(targetPrice),
				stockTargetPrice))
			.toList();
	}

	protected List<StockDividend> createStockDividendWith(String tickerSymbol) {
		return TestDataFactory.createStockDividend(tickerSymbol);
	}

	protected List<StockDividend> createStockDividendThisYearWith(String tickerSymbol) {
		return TestDataFactory.createStockDividendThisYearWith(tickerSymbol);
	}

	protected Cookie[] createTokenCookies() {
		TokenFactory tokenFactory = new TokenFactory(cookieDomainProvider);
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

	public void setAuthentication(Member member) {
		Set<String> roleNames = roleRepository.findAllById(member.getRoleIds()).stream()
			.map(Role::getRoleName)
			.collect(Collectors.toSet());
		MemberAuthentication memberAuthentication = MemberAuthentication.from(member, roleNames);
		UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
			memberAuthentication,
			Strings.EMPTY,
			memberAuthentication.getSimpleGrantedAuthority()
		);
		SecurityContextHolder.getContext().setAuthentication(authenticationToken);
	}
}
