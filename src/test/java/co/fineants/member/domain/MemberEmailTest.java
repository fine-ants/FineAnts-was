package co.fineants.member.domain;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class MemberEmailTest {
	@ParameterizedTest
	@MethodSource(value = "co.fineants.TestDataProvider#validEmailValues")
	void canCreated(String value) {
		MemberEmail memberEmail = new MemberEmail(value);

		Assertions.assertThat(memberEmail).isNotNull();
	}

	@ParameterizedTest
	@MethodSource(value = "co.fineants.TestDataProvider#invalidEmailValues")
	void newInstance_whenInvalidEmail_thenThrowException(String value) {
		Throwable throwable = Assertions.catchThrowable(() -> new MemberEmail(value));

		Assertions.assertThat(throwable)
			.isInstanceOf(IllegalArgumentException.class);
	}
}
