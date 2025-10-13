package co.fineants.api.domain.member.domain.entity;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import co.fineants.api.global.errors.exception.business.EmailInvalidInputException;

class MemberEmailTest {
	@Test
	void canCreated() {
		MemberEmail memberEmail = new MemberEmail("ant1234@gmail.com");

		Assertions.assertThat(memberEmail).isNotNull();
	}

	@ParameterizedTest
	@MethodSource(value = "co.fineants.TestDataProvider#invalidEmailValues")
	void newInstance_whenInvalidEmail_thenThrowException(String value) {
		Throwable throwable = Assertions.catchThrowable(() -> new MemberEmail(value));

		Assertions.assertThat(throwable)
			.isInstanceOf(EmailInvalidInputException.class);
	}
}
