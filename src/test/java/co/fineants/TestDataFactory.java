package co.fineants;

import java.time.LocalDateTime;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import co.fineants.api.domain.kis.client.KisAccessToken;
import co.fineants.api.domain.member.domain.entity.Member;
import co.fineants.api.domain.member.domain.entity.MemberProfile;
import co.fineants.api.domain.member.domain.entity.Role;

public final class TestDataFactory {
	private TestDataFactory() {

	}

	public static KisAccessToken createKisAccessToken() {
		final int EXPIRED_SECONDS = 86400;
		return KisAccessToken.bearerType("accessToken", LocalDateTime.now().plusSeconds(EXPIRED_SECONDS),
			EXPIRED_SECONDS);
	}

	public static Role createRole(String roleName, String roleDesc) {
		return Role.create(roleName, roleDesc);
	}

	public static Member createMember() {
		return createMember("nemo1234");
	}

	public static Member createMember(String nickname) {
		return createMember(nickname, "dragonbead95@naver.com");
	}

	public static Member createMember(String nickname, String email) {
		PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
		String password = passwordEncoder.encode("nemo1234@");
		MemberProfile profile = MemberProfile.localMemberProfile(email, nickname, password, "profileUrl");
		return Member.localMember(profile);
	}

	// public static Member createOauthMember() {
	// 	Role userRole = roleRepository.findRoleByRoleName("ROLE_USER")
	// 		.orElseThrow(() -> new FineAntsException(RoleErrorCode.NOT_EXIST_ROLE));
	// 	MemberProfile profile = MemberProfile.oauthMemberProfile("fineants1234@gmail.com", "fineants1234", "google",
	// 		"profileUrl1");
	// 	// 회원 생성
	// 	Member member = Member.oauthMember(profile);
	// 	// 역할 설정
	// 	member.addMemberRole(MemberRole.of(member, userRole));
	//
	// 	// 계정 알림 설정
	// 	member.setNotificationPreference(NotificationPreference.allActive());
	// 	return member;
	// }
	//
	// public static NotificationPreference createNotificationPreference(boolean browserNotify, boolean targetGainNotify,
	// 	boolean maxLossNotify, boolean targetPriceNotify) {
	// 	return NotificationPreference.create(
	// 		browserNotify,
	// 		targetGainNotify,
	// 		maxLossNotify,
	// 		targetPriceNotify
	// 	);
	// }
	//
	// public static Portfolio createPortfolio(Member member) {
	// 	return createPortfolio(
	// 		member,
	// 		Money.won(1000000)
	// 	);
	// }
	//
	// public static Portfolio createPortfolio(Member member, String name) {
	// 	return createPortfolio(
	// 		member,
	// 		name,
	// 		Money.won(1000000L),
	// 		Money.won(1500000L),
	// 		Money.won(900000L)
	// 	);
	// }
	//
	// public static Portfolio createPortfolio(Member member, Money budget) {
	// 	return createPortfolio(
	// 		member,
	// 		"내꿈은 워렌버핏",
	// 		budget,
	// 		Money.won(1500000L),
	// 		Money.won(900000L)
	// 	);
	// }
	//
	// public static Portfolio createPortfolio(Member member, String name, Money budget, Money targetGain,
	// 	Money maximumLoss) {
	// 	PortfolioDetail detail = PortfolioDetail.of(name, "토스증권", properties);
	// 	PortfolioFinancial financial = PortfolioFinancial.of(budget, targetGain, maximumLoss);
	// 	return Portfolio.allActive(
	// 		null,
	// 		detail,
	// 		financial,
	// 		member
	// 	);
	// }
	//
	// public static Stock createSamsungStock() {
	// 	return Stock.of("005930", "삼성전자보통주", "SamsungElectronics", "KR7005930003", "전기전자", Market.KOSPI);
	// }
	//
	// public static Stock createDongwhaPharmStock() {
	// 	return Stock.of("000020", "동화약품보통주", "DongwhaPharm", "KR7000020008", "의약품", Market.KOSPI);
	// }
	//
	// public static Stock createCcsStack() {
	// 	return Stock.of("066790", "씨씨에스충북방송", "KOREA CABLE T.V CHUNG-BUK SYSTEM CO.,LTD.", "KR7066790007", "방송서비스",
	// 		Market.KOSDAQ);
	// }
	//
	// /**
	//  * 해당 종목은 상장 폐지된 종목입니다.
	//  * @return 상장 폐지된 종목
	//  */
	// public static Stock createNokwonCI() {
	// 	return Stock.of("065560", "녹원씨엔아이", "Nokwon Commercials & Industries, Inc.", "KR7065560005", "소프트웨어",
	// 		Market.KOSDAQ);
	// }
	//
	// public static Stock createKakaoStock() {
	// 	return Stock.of("035720", "카카오보통주", "Kakao", "KR7035720002", "서비스업", Market.KOSPI);
	// }
	//
	// public static PortfolioHolding createPortfolioHolding(Portfolio portfolio, Stock stock) {
	// 	return PortfolioHolding.of(portfolio, stock);
	// }
	//
	// public static StockDividend createStockDividend(LocalDate recordDate, LocalDate paymentDate, Stock stock) {
	// 	LocalDate exDividendDate = exDividendDateCalculator.calculate(recordDate);
	// 	return StockDividend.create(Money.won(361), recordDate, exDividendDate, paymentDate, stock);
	// }
	//
	// public static StockDividend createStockDividend(Money dividend, LocalDate recordDate,
	// 	LocalDate paymentDate, Stock stock) {
	// 	LocalDate exDividendDate = exDividendDateCalculator.calculate(recordDate);
	// 	return StockDividend.create(dividend, recordDate, exDividendDate, paymentDate, stock);
	// }
	//
	// public static PurchaseHistory createPurchaseHistory(Long id, LocalDateTime purchaseDate, Count numShares,
	// 	Money purchasePricePerShare, String memo, PortfolioHolding portfolioHolding) {
	// 	return PurchaseHistory.create(id, purchaseDate, numShares, purchasePricePerShare, memo, portfolioHolding);
	// }
	//
	// public static FcmToken createFcmToken(String token, Member member) {
	// 	return FcmToken.create(member, token);
	// }
	//
	// public static WatchList createWatchList(Member member) {
	// 	return createWatchList("관심 종목1", member);
	// }
	//
	// public static WatchList createWatchList(String name, Member member) {
	// 	return WatchList.newWatchList(name, member);
	// }
	//
	// public static WatchStock createWatchStock(WatchList watchList, Stock stock) {
	// 	return WatchStock.newWatchStock(watchList, stock);
	// }
	//
	// public static StockTargetPrice createStockTargetPrice(Member member, Stock stock) {
	// 	return StockTargetPrice.newStockTargetPriceWithActive(member, stock);
	// }
	//
	// public static TargetPriceNotification createTargetPriceNotification(StockTargetPrice stockTargetPrice) {
	// 	return TargetPriceNotification.newTargetPriceNotification(Money.won(60000L), stockTargetPrice);
	// }
	//
	// public static List<TargetPriceNotification> createTargetPriceNotification(StockTargetPrice stockTargetPrice,
	// 	List<Long> targetPrices) {
	// 	return targetPrices.stream()
	// 		.map(targetPrice -> TargetPriceNotification.newTargetPriceNotification(Money.won(targetPrice),
	// 			stockTargetPrice))
	// 		.toList();
	// }
	//
	// public static List<StockDividend> createStockDividendWith(Stock stock) {
	// 	return List.of(
	// 		createStockDividend(
	// 			LocalDate.of(2022, 12, 31),
	// 			LocalDate.of(2023, 4, 14),
	// 			stock),
	// 		createStockDividend(
	// 			LocalDate.of(2023, 3, 31),
	// 			LocalDate.of(2023, 5, 17),
	// 			stock),
	// 		createStockDividend(
	// 			LocalDate.of(2023, 6, 30),
	// 			LocalDate.of(2023, 8, 16),
	// 			stock),
	// 		createStockDividend(
	// 			LocalDate.of(2023, 9, 30),
	// 			LocalDate.of(2023, 11, 20),
	// 			stock),
	// 		createStockDividend(
	// 			LocalDate.of(2024, 3, 31),
	// 			LocalDate.of(2024, 5, 17),
	// 			stock),
	// 		createStockDividend(
	// 			LocalDate.of(2024, 6, 30),
	// 			LocalDate.of(2024, 8, 16),
	// 			stock),
	// 		createStockDividend(
	// 			LocalDate.of(2024, 9, 30),
	// 			LocalDate.of(2024, 11, 20),
	// 			stock)
	// 	);
	// }
	//
	// public static List<StockDividend> createStockDividendThisYearWith(Stock stock) {
	// 	return List.of(
	// 		createStockDividend(
	// 			LocalDate.of(2024, 3, 31),
	// 			LocalDate.of(2024, 5, 17),
	// 			stock),
	// 		createStockDividend(
	// 			LocalDate.of(2024, 6, 30),
	// 			LocalDate.of(2024, 8, 16),
	// 			stock),
	// 		createStockDividend(
	// 			LocalDate.of(2024, 9, 30),
	// 			LocalDate.of(2024, 11, 20),
	// 			stock)
	// 	);
	// }
	//
	// public static Cookie[] createTokenCookies() {
	// 	TokenFactory tokenFactory = new TokenFactory(cookieDomainProvider);
	// 	Token token = Token.create("accessToken", "refreshToken");
	// 	ResponseCookie accessTokenCookie = tokenFactory.createAccessTokenCookie(token);
	// 	ResponseCookie refreshTokenCookie = tokenFactory.createRefreshTokenCookie(token);
	// 	return new Cookie[] {convertCookie(accessTokenCookie), convertCookie(refreshTokenCookie)};
	// }
	//
	// private static Cookie convertCookie(ResponseCookie cookie) {
	// 	String cookieString = cookie.toString();
	// 	int start = cookieString.indexOf("=") + 1;
	// 	return new Cookie(cookie.getName(), cookieString.substring(start));
	// }
	//
	// public static void setAuthentication(Member member) {
	// 	MemberAuthentication memberAuthentication = MemberAuthentication.from(member);
	// 	UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
	// 		memberAuthentication,
	// 		Strings.EMPTY,
	// 		memberAuthentication.getSimpleGrantedAuthority()
	// 	);
	// 	SecurityContextHolder.getContext().setAuthentication(authenticationToken);
	// }
}
