package co.fineants.api.domain.member.service;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import co.fineants.AbstractContainerBaseTest;

class EmailDuplicateValidatorTest extends AbstractContainerBaseTest {

	@Autowired
	private EmailDuplicateValidator validator;

	@Test
	void hasMemberWith_whenExistMember_thenReturnTrue() {
		String provider = "local";
		String email = "dragonbead95@naver.com";

		boolean actual = validator.hasMemberWith(email, provider);

		Assertions.assertThat(actual).isTrue();
	}
}
