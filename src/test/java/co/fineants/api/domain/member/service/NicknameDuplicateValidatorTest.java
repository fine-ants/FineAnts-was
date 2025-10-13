package co.fineants.api.domain.member.service;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class NicknameDuplicateValidatorTest {

	@Test
	void isDuplicate_whenDuplicatedNickname_thenReturnTrue() {
		NicknameDuplicateValidator validator = new NicknameDuplicateValidator();
		String nickname = "ants1234";

		boolean actual = validator.isDuplicate(nickname);

		Assertions.assertThat(actual).isTrue();
	}
}
