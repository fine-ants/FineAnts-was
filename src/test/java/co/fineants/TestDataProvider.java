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
			Arguments.of("개미1234")
		);
	}
}
