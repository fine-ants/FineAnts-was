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
			Arguments.of("a".repeat(100))
		);
	}

	public static Stream<Arguments> invalidNicknameValues() {
		return Stream.of(
			Arguments.of((Object)null),
			Arguments.of(""),
			Arguments.of(" "),
			Arguments.of("a".repeat(101)),
			Arguments.of("Invalid@Name!"),
			Arguments.of("Name With Spaces"),
			Arguments.of("Special#Char$")
		);
	}
}
