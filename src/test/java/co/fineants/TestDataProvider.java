package co.fineants;

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
}
