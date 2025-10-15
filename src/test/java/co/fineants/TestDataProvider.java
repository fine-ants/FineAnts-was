package co.fineants;

import static co.fineants.TestDataFactory.*;

import java.util.stream.Stream;

import org.junit.jupiter.params.provider.Arguments;

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
}
