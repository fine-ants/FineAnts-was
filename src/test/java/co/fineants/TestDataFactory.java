package co.fineants;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.core.io.ClassPathResource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.multipart.MultipartFile;

import co.fineants.api.domain.common.count.Count;
import co.fineants.api.domain.common.money.Money;
import co.fineants.api.domain.dividend.domain.calculator.ExDividendDateCalculator;
import co.fineants.api.domain.dividend.domain.calculator.FileExDividendDateCalculator;
import co.fineants.api.domain.dividend.domain.entity.DividendDates;
import co.fineants.api.domain.dividend.domain.reader.HolidayFileReader;
import co.fineants.api.domain.holding.domain.entity.PortfolioHolding;
import co.fineants.api.domain.kis.client.KisAccessToken;
import co.fineants.api.domain.kis.repository.FileHolidayRepository;
import co.fineants.api.domain.member.domain.entity.Member;
import co.fineants.api.domain.member.domain.entity.MemberProfile;
import co.fineants.api.domain.member.domain.entity.Nickname;
import co.fineants.api.domain.member.domain.entity.NotificationPreference;
import co.fineants.api.domain.portfolio.domain.entity.Portfolio;
import co.fineants.api.domain.portfolio.domain.entity.PortfolioDetail;
import co.fineants.api.domain.portfolio.domain.entity.PortfolioFinancial;
import co.fineants.api.domain.portfolio.properties.PortfolioProperties;
import co.fineants.api.domain.purchasehistory.domain.entity.PurchaseHistory;
import co.fineants.api.domain.role.domain.Role;
import co.fineants.api.domain.stock.domain.entity.Market;
import co.fineants.api.domain.stock.domain.entity.Stock;
import co.fineants.api.domain.stock.domain.entity.StockDividend;

public final class TestDataFactory {

	private static final ExDividendDateCalculator exDividendDateCalculator = new FileExDividendDateCalculator(
		new FileHolidayRepository(new HolidayFileReader()));

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

		MemberProfile profile = MemberProfile.localMemberProfile(email, new Nickname(nickname), password, "profileUrl");
		NotificationPreference notificationPreference = NotificationPreference.allActive();
		return Member.createMember(profile, notificationPreference);
	}

	public static Portfolio createPortfolio(Member member) {
		return createPortfolio(
			member,
			Money.won(1000000)
		);
	}

	public static Portfolio createPortfolio(Member member, Money budget) {
		return createPortfolio(
			member,
			"내꿈은 워렌버핏",
			budget,
			Money.won(1500000L),
			Money.won(900000L)
		);
	}

	public static Portfolio createPortfolio(Member member, String name, Money budget, Money targetGain,
		Money maximumLoss) {
		PortfolioProperties properties = new PortfolioProperties(new String[] {"토스증권"});
		PortfolioDetail detail = PortfolioDetail.of(name, "토스증권", properties);
		PortfolioFinancial financial = PortfolioFinancial.of(budget, targetGain, maximumLoss);
		return Portfolio.allActive(
			null,
			detail,
			financial,
			member
		);
	}

	public static Portfolio createPortfolio(Member member, String name) {
		return createPortfolio(
			member,
			name,
			Money.won(1000000L),
			Money.won(1500000L),
			Money.won(900000L)
		);
	}

	public static Stock createSamsungStock() {
		return Stock.of("005930", "삼성전자보통주", "SamsungElectronics", "KR7005930003", "전기,전자", Market.KOSPI);
	}

	public static Stock createDongwhaPharmStock() {
		return Stock.of("000020", "동화약품보통주", "DongwhaPharm", "KR7000020008", "의약품", Market.KOSPI);
	}

	public static Stock createKakaoStock() {
		return Stock.of("035720", "카카오보통주", "Kakao", "KR7035720002", "서비스업", Market.KOSPI);
	}

	public static MultipartFile createProfileFile() {
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

	public static MultipartFile createOverSizeMockProfileFile() {
		byte[] profile = new byte[3145728];
		return new MockMultipartFile("profileImageFile", "profile.jpeg", "image/jpeg", profile);
	}

	public static StockDividend createKakaoStockDividend() {
		Money dividend = Money.won(68);
		LocalDate recordDate = LocalDate.of(2025, 3, 10);
		LocalDate exDividendDate = LocalDate.of(2025, 3, 7);
		LocalDate paymentDate = LocalDate.of(2025, 4, 24);
		DividendDates dividendDates = DividendDates.of(recordDate, exDividendDate, paymentDate);
		String tickerSymbol = "035720";
		return new StockDividend(
			dividend,
			dividendDates,
			false,
			tickerSymbol
		);
	}

	public static StockDividend createSamsungStockDividend() {
		Money dividend = Money.won(361);
		LocalDate recordDate = LocalDate.of(2023, 3, 31);
		LocalDate exDividendDate = LocalDate.of(2023, 3, 30);
		LocalDate paymentDate = LocalDate.of(2023, 5, 17);
		DividendDates dividendDates = DividendDates.of(recordDate, exDividendDate, paymentDate);
		String tickerSymbol = "005930";
		return new StockDividend(
			dividend,
			dividendDates,
			false,
			tickerSymbol
		);
	}

	public static PurchaseHistory createPurchaseHistory(LocalDate purchaseDate, PortfolioHolding holding) {
		Count numShares = Count.from(5);
		Money purchasePerShare = Money.won(10000);
		String memo = "첫구매";
		return PurchaseHistory.create(purchaseDate.atStartOfDay(), numShares, purchasePerShare, memo, holding);
	}

	public static PortfolioHolding createPortfolioHolding(Portfolio portfolio, Stock stock) {
		return PortfolioHolding.of(portfolio, stock);
	}

	public static List<StockDividend> createStockDividend(String tickerSymbol) {
		return List.of(
			createStockDividend(
				Money.won(361L),
				LocalDate.of(2022, 12, 31),
				LocalDate.of(2023, 4, 14),
				tickerSymbol
			),
			createStockDividend(
				Money.won(361L),
				LocalDate.of(2023, 3, 31),
				LocalDate.of(2023, 5, 17),
				tickerSymbol
			),
			createStockDividend(
				Money.won(361L),
				LocalDate.of(2023, 6, 30),
				LocalDate.of(2023, 8, 16),
				tickerSymbol
			),
			createStockDividend(
				Money.won(361L),
				LocalDate.of(2023, 9, 30),
				LocalDate.of(2023, 11, 20),
				tickerSymbol
			),
			createStockDividend(
				Money.won(361L),
				LocalDate.of(2024, 3, 31),
				LocalDate.of(2024, 5, 17),
				tickerSymbol
			),
			createStockDividend(
				Money.won(361L),
				LocalDate.of(2024, 6, 30),
				LocalDate.of(2024, 8, 16),
				tickerSymbol
			),
			createStockDividend(
				Money.won(361L),
				LocalDate.of(2024, 9, 30),
				LocalDate.of(2024, 11, 20),
				tickerSymbol
			)
		);
	}

	public static List<StockDividend> createSamsungStockDividends() {
		String tickerSymbol = "005930";
		Money dividend = Money.won(361L);
		return List.of(
			createStockDividend(
				dividend,
				LocalDate.of(2022, 3, 31),
				LocalDate.of(2022, 5, 17),
				tickerSymbol
			),
			createStockDividend(
				dividend,
				LocalDate.of(2022, 6, 30),
				LocalDate.of(2022, 8, 16),
				tickerSymbol
			),
			createStockDividend(
				dividend,
				LocalDate.of(2022, 9, 30),
				LocalDate.of(2022, 11, 15),
				tickerSymbol
			),
			createStockDividend(
				dividend,
				LocalDate.of(2022, 12, 31),
				LocalDate.of(2023, 4, 14),
				tickerSymbol
			),
			createStockDividend(
				dividend,
				LocalDate.of(2023, 3, 31),
				LocalDate.of(2023, 5, 17),
				tickerSymbol
			),
			createStockDividend(
				dividend,
				LocalDate.of(2023, 6, 30),
				LocalDate.of(2023, 8, 16),
				tickerSymbol
			),
			createStockDividend(
				dividend,
				LocalDate.of(2023, 9, 30),
				LocalDate.of(2023, 11, 20),
				tickerSymbol
			),
			createStockDividend(
				dividend,
				LocalDate.of(2023, 12, 31),
				LocalDate.of(2024, 4, 19),
				tickerSymbol
			),
			createStockDividend(
				dividend,
				LocalDate.of(2024, 3, 31),
				null,
				tickerSymbol
			)
		);
	}

	private static StockDividend createStockDividend(Money dividend, LocalDate recordDate, LocalDate paymentDate,
		String tickerSymbol) {
		LocalDate exDividendDate = exDividendDateCalculator.calculate(recordDate);
		DividendDates dividendDates = DividendDates.of(recordDate, exDividendDate, paymentDate);
		return new StockDividend(dividend, dividendDates, false, tickerSymbol);
	}

	public static List<StockDividend> createStockDividendThisYearWith(String tickerSymbol) {
		Money dividend = Money.won(361L);
		return List.of(
			createStockDividend(
				dividend,
				LocalDate.of(2024, 3, 31),
				LocalDate.of(2024, 5, 17),
				tickerSymbol
			),
			createStockDividend(
				dividend,
				LocalDate.of(2024, 6, 30),
				LocalDate.of(2024, 8, 16),
				tickerSymbol
			),
			createStockDividend(
				dividend,
				LocalDate.of(2024, 9, 30),
				LocalDate.of(2024, 11, 20),
				tickerSymbol
			)
		);
	}

	public static List<StockDividend> createKakaoStockDividends() {
		String tickerSymbol = "035720";
		return List.of(
			createStockDividend(
				Money.won(61L),
				LocalDate.of(2022, 12, 31),
				LocalDate.of(2023, 4, 25),
				tickerSymbol
			),
			createStockDividend(
				Money.won(61L),
				LocalDate.of(2024, 2, 29),
				null,
				tickerSymbol
			)
		);
	}
}
