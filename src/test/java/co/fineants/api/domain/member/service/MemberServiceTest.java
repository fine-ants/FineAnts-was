package co.fineants.api.domain.member.service;

import static co.fineants.TestDataFactory.*;
import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import co.fineants.AbstractContainerBaseTest;
import co.fineants.api.domain.common.count.Count;
import co.fineants.api.domain.common.money.Money;
import co.fineants.api.domain.holding.domain.entity.PortfolioHolding;
import co.fineants.api.domain.holding.repository.PortfolioHoldingRepository;
import co.fineants.api.domain.member.domain.dto.request.ProfileChangeServiceRequest;
import co.fineants.api.domain.member.domain.dto.response.ProfileChangeResponse;
import co.fineants.api.domain.member.domain.dto.response.ProfileResponse;
import co.fineants.member.domain.Member;
import co.fineants.api.domain.member.repository.MemberRepository;
import co.fineants.api.domain.portfolio.domain.entity.Portfolio;
import co.fineants.api.domain.portfolio.repository.PortfolioRepository;
import co.fineants.api.domain.purchasehistory.repository.PurchaseHistoryRepository;
import co.fineants.api.domain.stock.domain.entity.Stock;
import co.fineants.api.domain.stock.repository.StockRepository;
import co.fineants.api.domain.stock_target_price.domain.entity.StockTargetPrice;
import co.fineants.api.domain.stock_target_price.repository.StockTargetPriceRepository;
import co.fineants.api.domain.stock_target_price.repository.TargetPriceNotificationRepository;
import co.fineants.api.domain.watchlist.domain.entity.WatchList;
import co.fineants.api.domain.watchlist.repository.WatchListRepository;
import co.fineants.api.domain.watchlist.repository.WatchStockRepository;
import co.fineants.api.global.errors.exception.business.MemberProfileNotChangeException;
import co.fineants.api.global.errors.exception.business.NicknameDuplicateException;

class MemberServiceTest extends AbstractContainerBaseTest {

	@Autowired
	private MemberService memberService;

	@Autowired
	private MemberRepository memberRepository;

	@Autowired
	private PortfolioRepository portfolioRepository;

	@Autowired
	private PortfolioHoldingRepository portfolioHoldingRepository;

	@Autowired
	private PurchaseHistoryRepository purchaseHistoryRepository;

	@Autowired
	private StockTargetPriceRepository stockTargetPriceRepository;

	@Autowired
	private TargetPriceNotificationRepository targetPriceNotificationRepository;
	@Autowired
	private StockRepository stockRepository;

	@Autowired
	private WatchListRepository watchListRepository;

	@Autowired
	private WatchStockRepository watchStockRepository;

	public static Stream<Arguments> validChangeProfileSource() {
		return Stream.of(
			Arguments.of(createProfileFile(), "nemo12345", "nemo12345", "새 프로필 사진과 새 닉네임 변경"),
			Arguments.of(createProfileFile(), null, "nemo1234", "새 프로필 사진만 변경"),
			Arguments.of(createEmptyProfileImageFile(), null, "nemo1234", "기본 프로필 사진으로만 변경"),
			Arguments.of(null, "nemo12345", "nemo12345", "닉네임만 변경"),
			Arguments.of(createProfileFile(), "nemo1234", "nemo1234", "프로필 사진과 닉네임을 그대로 유지")
		);
	}

	private static MultipartFile createEmptyProfileImageFile() {
		return new MockMultipartFile("profileImageFile", new byte[] {});
	}

	@DisplayName("프로필 이미지와 닉네임이 주어진 상태에서 사용자의 프로필 정보를 변경한다")
	@ParameterizedTest
	@MethodSource(value = "validChangeProfileSource")
	void givenProfileImageFileAndNickname_whenChangeProfile_thenChangedProfileInfo(
		MultipartFile profileImageFile,
		String nickname,
		String expectedNickname) {
		// given
		Member member = memberRepository.save(createMember());
		ProfileChangeServiceRequest serviceRequest = ProfileChangeServiceRequest.of(
			profileImageFile,
			nickname,
			member.getId()
		);
		// when
		ProfileChangeResponse response = memberService.changeProfile(serviceRequest);

		// then
		assertThat(response)
			.extracting("user")
			.extracting("nickname")
			.isEqualTo(expectedNickname);
	}

	@DisplayName("사용자는 회원 프로필에서 닉네임 변경시 중복되어 변경하지 못한다")
	@Test
	void changeProfile_whenDuplicateNickname_thenThrowException() {
		// given
		memberRepository.save(createMember("nemo12345"));
		Member member = memberRepository.save(createMember());
		ProfileChangeServiceRequest serviceRequest = ProfileChangeServiceRequest.of(
			null,
			"nemo12345",
			member.getId()
		);

		// when
		Throwable throwable = catchThrowable(() -> memberService.changeProfile(serviceRequest));

		// then
		String expected = "nemo12345";
		assertThat(throwable)
			.isInstanceOf(NicknameDuplicateException.class)
			.hasMessage(expected);
	}

	@DisplayName("사용자는 회원 프로필에서 변경할 정보가 없어서 실패한다")
	@Test
	void changeProfile_whenNoChangeProfile_thenThrowException() {
		// given
		Member member = memberRepository.save(createMember());
		ProfileChangeServiceRequest serviceRequest = ProfileChangeServiceRequest.of(
			null,
			null,
			member.getId()
		);

		// when
		Throwable throwable = catchThrowable(() -> memberService.changeProfile(serviceRequest));

		// then
		assertThat(throwable)
			.isInstanceOf(MemberProfileNotChangeException.class)
			.hasMessage(serviceRequest.toString());
	}

	@DisplayName("사용자는 프로필을 조회합니다.")
	@Test
	void readProfile() {
		// given
		Member member = memberRepository.save(createMember());

		// when
		ProfileResponse response = memberService.readProfile(member.getId());

		// then
		assertThat(response)
			.extracting("user")
			.extracting("id", "nickname", "email", "profileUrl")
			.containsExactlyInAnyOrder(member.getId(), "nemo1234", "dragonbead95@naver.com", "profileUrl");
		assertThat(response)
			.extracting("user.notificationPreferences")
			.extracting("browserNotify", "targetGainNotify", "maxLossNotify", "targetPriceNotify")
			.containsExactlyInAnyOrder(true, true, true, true);
	}

	@DisplayName("사용자는 계정을 삭제한다")
	@Test
	void deleteMember() {
		// given
		Member member = memberRepository.save(createMember());
		Portfolio portfolio = portfolioRepository.save(createPortfolio(member));
		Stock stock = stockRepository.save(createSamsungStock());
		createStockDividendWith(stock.getTickerSymbol()).forEach(stock::addStockDividend);
		PortfolioHolding portfolioHolding = portfolioHoldingRepository.save(createPortfolioHolding(portfolio, stock));

		LocalDateTime purchaseDate = LocalDateTime.of(2023, 9, 26, 9, 30, 0);
		Count numShares = Count.from(3);
		Money purchasePricePerShare = Money.won(50000);
		String memo = "첫구매";
		purchaseHistoryRepository.save(
			createPurchaseHistory(null, purchaseDate, numShares, purchasePricePerShare, memo, portfolioHolding));
		StockTargetPrice stockTargetPrice = stockTargetPriceRepository.save(createStockTargetPrice(member, stock));
		targetPriceNotificationRepository.save(createTargetPriceNotification(stockTargetPrice));
		WatchList watchList = watchListRepository.save(createWatchList(member));
		watchStockRepository.save(createWatchStock(watchList, stock));
		// when
		memberService.deleteMember(member.getId());

		// then
		assertThat(memberRepository.findById(member.getId())).isEmpty();
	}

	@DisplayName("두 비밀번호가 일치하여 예외가 발생하지 않는다")
	@Test
	void givenPassword_whenVerifyPasswordMatch_thenNotThrowException() {
		// given
		String password = "nemo1234@";
		String passwordConfirm = "nemo1234@";
		// when & then
		assertDoesNotThrow(() -> memberService.verifyPasswordMatch(password, passwordConfirm));
	}
}
