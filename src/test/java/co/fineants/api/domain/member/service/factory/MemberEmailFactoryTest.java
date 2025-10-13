package co.fineants.api.domain.member.service.factory;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;

import co.fineants.AbstractContainerBaseTest;
import co.fineants.api.domain.member.domain.entity.MemberEmail;
import co.fineants.api.global.errors.exception.business.EmailInvalidInputException;

class MemberEmailFactoryTest extends AbstractContainerBaseTest {

	@Autowired
	private MemberEmailFactory factory;

	@ParameterizedTest
	@MethodSource(value = "co.fineants.TestDataProvider#validEmailValues")
	void create_whenValueIsValid_thenReturnMemberEmailInstance(String value) {
		MemberEmail memberEmail = factory.create(value);

		Assertions.assertThat(memberEmail).isNotNull();
	}

	@ParameterizedTest
	@MethodSource(value = "co.fineants.TestDataProvider#invalidEmailValues")
	void create_whenValueIsInvalid_thenThrowException(String value) {
		Throwable throwable = Assertions.catchThrowable(() -> factory.create(value));

		Assertions.assertThat(throwable)
			.isInstanceOf(EmailInvalidInputException.class)
			.hasMessage(value);
	}
}
