package co.fineants.api.domain.member.service;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class NicknameDuplicateValidatorTest {

	@Test
	void verify() {
		NicknameDuplicateValidator validator = new NicknameDuplicateValidator();
		String nickname = "ants1234";

		boolean actual = validator.verify(nickname);

		Assertions.assertThat(actual).isFalse();
	}
}
