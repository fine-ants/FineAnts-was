package co.fineants.member.domain;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class NicknameTest {

	@ParameterizedTest
	@MethodSource(value = "co.fineants.TestDataProvider#validNicknameValues")
	void create_whenNicknameValueIsValid_thenNicknameIsNotNull(String value) {
		Nickname nickname = new Nickname(value);

		Assertions.assertThat(nickname).isNotNull();
	}

	@ParameterizedTest
	@MethodSource(value = "co.fineants.TestDataProvider#invalidNicknameValues")
	void create_whenNicknameValueIsInvalid_thenThrowException(String value) {
		Throwable throwable = Assertions.catchThrowable(() -> new Nickname(value));

		Assertions.assertThat(throwable)
			.isInstanceOf(IllegalArgumentException.class);
	}

}
