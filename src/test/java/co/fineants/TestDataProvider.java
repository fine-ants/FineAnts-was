package co.fineants;

import static co.fineants.TestDataFactory.*;

import java.util.Collections;
import java.util.Date;
import java.util.stream.Stream;

import org.junit.jupiter.params.provider.Arguments;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import co.fineants.api.domain.common.money.Money;

public class TestDataProvider {

	public static Stream<Arguments> validNicknameValues() {
		return Stream.of(
			Arguments.of("User123"),
			Arguments.of("AlphaBeta"),
			Arguments.of("CoolGuy99"),
			Arguments.of("JaneDoe"),
			Arguments.of("개미1234"),
			Arguments.of("aa"),
			Arguments.of("a".repeat(100)),
			Arguments.of("00")
		);
	}

	public static Stream<Arguments> invalidNicknameValues() {
		return Stream.of(
			Arguments.of((Object)null),
			Arguments.of(""),
			Arguments.of(" "),
			Arguments.of("a"),
			Arguments.of("a".repeat(101)),
			Arguments.of("Invalid@Name!"),
			Arguments.of("Name With Spaces"),
			Arguments.of("Special#Char$")
		);
	}

	public static Stream<Arguments> validEmailValues() {
		return Stream.of(
			Arguments.of("user@example.com"),
			Arguments.of("john.doe123@domain.co"),
			Arguments.of("alice_smith@company.org"),
			Arguments.of("test.email+label@gmail.com"),
			Arguments.of("my-account_01@sub.domain.net"),
			Arguments.of("hello.world@my-site.io"),
			Arguments.of("admin@service.info"),
			Arguments.of("simple123@abc.xyz"),
			Arguments.of("contact@domain.co.kr"),
			Arguments.of("support_team@company123.biz")
		);
	}

	public static Stream<Arguments> invalidEmailValues() {
		return Stream.of(
			Arguments.of((Object)null),
			Arguments.of(""),
			Arguments.of(" "),
			Arguments.of("a"),
			Arguments.of("a".repeat(101)),
			Arguments.of("Invalid@Name!"),
			Arguments.of("Name With Spaces"),
			Arguments.of("Special#Char$"),
			Arguments.of("@example.com"), // 로컬 파트 없음
			Arguments.of("user@.com"), // 도메인 이름 없음
			Arguments.of("user@domain"), // 최상위 도메인(TLD)이 없음
			Arguments.of("user@domain.toolongtld"), // TLD가 6자를 초과함
			Arguments.of("user name@example.com"), // 공백 포함
			Arguments.of("username@example..com") // 연속된 마침표
		);
	}

	public static Stream<Arguments> validChangeProfileSource() {
		return Stream.of(
			Arguments.of(createProfileFile(), "nemo12345", "nemo12345", "새 프로필 사진과 새 닉네임 변경"),
			Arguments.of(createProfileFile(), null, "nemo1234", "새 프로필 사진만 변경"),
			Arguments.of(createEmptyMockMultipartFile(), null, "nemo1234", "기본 프로필 사진으로만 변경"),
			Arguments.of(null, "nemo12345", "nemo12345", "닉네임만 변경"),
			Arguments.of(createProfileFile(), "nemo1234", "nemo1234", "프로필 사진과 닉네임을 그대로 유지")
		);
	}

	public static Stream<Arguments> invalidEmptySignupData() {
		String[] expectedFields = {"nickname", "email", "password", "passwordConfirm", "nickname", "email", "password",
			"passwordConfirm"};
		String[] expectedDefaultMessages = {
			"닉네임은 필수 정보입니다",
			"이메일은 필수 정보입니다",
			"비밀번호는 필수 정보입니다",
			"비밀번호 확인은 필수 정보입니다",
			"잘못된 입력 형식입니다",
			"잘못된 입력 형식입니다",
			"잘못된 입력 형식입니다",
			"잘못된 입력 형식입니다"
		};
		return Stream.of(
			Arguments.of("", "", "", "", expectedFields, expectedDefaultMessages),
			Arguments.of(" ", " ", " ", " ", expectedFields, expectedDefaultMessages),
			Arguments.of("  ", "  ", "  ", "  ", expectedFields, expectedDefaultMessages)
		);
	}

	public static Stream<Arguments> invalidSignupData() {
		String[] expectedFields = {"nickname", "email", "password", "passwordConfirm"};
		String[] expectedDefaultMessages = {"잘못된 입력 형식입니다", "잘못된 입력 형식입니다", "잘못된 입력 형식입니다", "잘못된 입력 형식입니다"};
		return Stream.of(
			Arguments.of("a", "a", "a", "a", expectedFields, expectedDefaultMessages)
		);
	}

	public static Stream<Arguments> validJwtTokenCreateDateSource() {
		Date now = new Date();
		long oneDayMilliSeconds = 1000 * 60 * 60 * 24; // 1일
		long oneHourMilliSeconds = 1000 * 60 * 60; // 1시간
		long oneMinuteMilliSeconds = 1000 * 60; // 1분
		long thirteenDaysMilliSeconds =
			oneDayMilliSeconds * 13 + oneHourMilliSeconds * 23 + oneMinuteMilliSeconds * 5; // 13일 23시간 5분
		Date now1 = new Date(now.getTime() - oneDayMilliSeconds);
		Date now2 = new Date(now.getTime() - thirteenDaysMilliSeconds);

		return Stream.of(
			Arguments.of(now1, now1),
			Arguments.of(now2, now2),
			Arguments.of(now, now2)
		);
	}

	public static Stream<Arguments> invalidJwtTokenCreateDateSource() {
		long fifteenDayMilliSeconds = 1000 * 60 * 60 * 24 * 15; // 1일
		Date now1 = new Date(fifteenDayMilliSeconds);
		return Stream.of(
			Arguments.of(now1, now1)
		);
	}

	public static Stream<Arguments> invalidCreatePortfolioSource() {
		return Stream.of(
			Arguments.of("", "", 0L, -1L, -1L)
		);
	}

	public static Stream<Arguments> createPortfolioSource() {
		return Stream.of(
			Arguments.of(1000000L, 1500000L, 900000L),
			Arguments.of(0L, 0L, 0L),
			Arguments.of(0L, 1500000L, 900000L)
		);
	}

	public static Stream<Arguments> invalidPortfolioHoldingIds() {
		return Stream.of(
			Arguments.of(Collections.emptyList()),
			Arguments.of((Object)null)
		);
	}

	public static Stream<Arguments> invalidTargetPrice() {
		return Stream.of(
			Arguments.of(null, Money.won(-1L)),
			Arguments.of(null, null)
		);
	}

	public static Stream<Arguments> invalidTargetPriceNotificationIds() {
		return Stream.of(
			Arguments.of(null, Collections.emptyList(), new String[] {
				"필수 정보입니다",
				"등록번호가 최소 1개 이상이어야 합니다"
			}),
			Arguments.of(null, null, new String[] {
				"필수 정보입니다",
				"필수 정보입니다"
			})
		);
	}

	public static Stream<Arguments> invalidDeleteWatchListIds() {
		return Stream.of(
			Arguments.of(null, new String[] {"필수 정보입니다"}),
			Arguments.of(Collections.emptyList(), new String[] {"최소 1개의 관심종목 식별자가 필요합니다"})
		);
	}

	public static Stream<Arguments> invalidWatchListNames() {
		return Stream.of(
			Arguments.of((Object)null),
			Arguments.of(""),
			Arguments.of("  ")
		);
	}

	public static Stream<Arguments> invalidCreateWatchStockRequests() {
		return Stream.of(
			Arguments.of(null, new String[] {"필수 정보입니다"}),
			Arguments.of(Collections.emptyList(), new String[] {"최소 1개의 종목의 티커심볼이 필요합니다"})
		);
	}

	public static Stream<Arguments> invalidDeleteWatchStockRequests() {
		return Stream.of(
			Arguments.of(null, new String[] {"필수 정보입니다"}),
			Arguments.of(Collections.emptyList(), new String[] {"최소 1개의 종목의 티커심볼이 필요합니다"})
		);
	}

	public static Stream<Arguments> validPasswords() {
		return Stream.of(
			Arguments.of("abcd1234!"),
			Arguments.of("Abcd1234@"),
			Arguments.of("A1!aaaaa"),
			Arguments.of("Passw0rd#"),
			Arguments.of("Qwer1234$"),
			Arguments.of("AAaa11!!"),
			Arguments.of("Hello123@"),
			Arguments.of("GoodPwd1*"),
			Arguments.of("Secure9#"),
			Arguments.of("Strong16$")
		);
	}

	public static Stream<Arguments> invalidPasswords() {
		return Stream.of(
			Arguments.of("abcdefg", "길이 < 8, 숫자 및 특수문자 없음"),
			Arguments.of("abcdefgh", "숫자 및 특수문자 없음"),
			Arguments.of("abcd1234", "특수문자 없음"),
			Arguments.of("abcd!@#$", "숫자 없음"),
			Arguments.of("12345678", "영문자 없음"),
			Arguments.of("abcd12!", "길이 < 8 (7자)"),
			Arguments.of("Ab1!", "길이 < 8, 너무 짧음"),
			Arguments.of("abcdefghijklmnopq1!", "길이 > 16 (17자 이상)"),
			Arguments.of("abcd 1234!", "공백 포함"),
			Arguments.of("abcd1234?", "허용되지 않은 특수문자 '?' 사용"),
			Arguments.of("abcd-1234!", "허용되지 않은 특수문자 '-' 사용"),
			Arguments.of("!!!!!!@@@@", "영문자 및 숫자 없음"),
			Arguments.of("Password!", "숫자 없음"),
			Arguments.of("1234567!", "영문자 없음, 길이 < 8"),
			Arguments.of("Passw0rd😊!", "허용되지 않은 이모지 포함")
		);
	}

	public static Stream<Arguments> invalidProfileFileSource() {
		MultipartFile emptyFile = new MockMultipartFile("file", "", "text/plain", new byte[0]); // 빈 파일
		return Stream.of(
			Arguments.of((Object)null, "null 파일"),
			Arguments.of(emptyFile, "빈 파일")
		);
	}
}
