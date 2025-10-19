package co.fineants.member.domain;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

import co.fineants.AbstractContainerBaseTest;

class MemberPasswordTest extends AbstractContainerBaseTest {

	@Autowired
	private PasswordEncoder passwordEncoder;

	@DisplayName("유효한 비밀번호로 MemberPassword 생성 가능")
	@ParameterizedTest
	@MethodSource(value = "co.fineants.TestDataProvider#validPasswords")
	void newInstance(String rawPassword) {
		MemberPassword memberPassword = new MemberPassword(rawPassword, passwordEncoder);

		Assertions.assertThat(memberPassword).isNotNull();
	}

	@DisplayName("유효하지 않은 비밀번호로 MemberPassword 생성 시도하면 예외 발생")
	@ParameterizedTest
	@MethodSource(value = "co.fineants.TestDataProvider#invalidPasswords")
	void newInstance_whenInvalidPassword_thenThrowException(String rawPassword, String ignoredReason) {
		Throwable throwable = Assertions.catchThrowable(() -> new MemberPassword(rawPassword, passwordEncoder));

		Assertions.assertThat(throwable)
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("Password format is invalid");
	}

	@DisplayName("이미 인코딩된 비밀번호로 MemberPassword 생성 시도하면 정규식을 만족하지 않아서 예외 발생")
	@Test
	void newInstance_whenEncodedPassword_thenThrowException() {
		String encodedPassword = passwordEncoder.encode("Password1!");

		Throwable throwable = Assertions.catchThrowable(
			() -> new MemberPassword(encodedPassword, passwordEncoder));

		Assertions.assertThat(throwable)
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("Password format is invalid");
	}
}
