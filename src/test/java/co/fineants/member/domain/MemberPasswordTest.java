package co.fineants.member.domain;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

import co.fineants.AbstractContainerBaseTest;

class MemberPasswordTest extends AbstractContainerBaseTest {

	@Autowired
	private PasswordEncoder passwordEncoder;

	@ParameterizedTest
	@MethodSource(value = "co.fineants.TestDataProvider#validPasswords")
	void newInstance(String rawPassword) {
		MemberPassword memberPassword = new MemberPassword(rawPassword, passwordEncoder);

		Assertions.assertThat(memberPassword).isNotNull();
	}

	@ParameterizedTest
	@MethodSource(value = "co.fineants.TestDataProvider#invalidPasswords")
	void newInstance_whenInvalidPassword_thenThrowException(String rawPassword, String reason) {
		Throwable throwable = Assertions.catchThrowable(() -> new MemberPassword(rawPassword, passwordEncoder));

		Assertions.assertThat(throwable)
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("Password format is invalid");
	}
}
