package codesquad.fineants.domain.member.service;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import codesquad.fineants.AbstractContainerBaseTest;

class NicknameGeneratorTest extends AbstractContainerBaseTest {

	@Autowired
	private NicknameGenerator generator;

	@DisplayName("회원의 랜덤 닉네임을 생성합니다")
	@Test
	void generate() {
		// given
		int expectedLength = 10;

		// when
		String nickname = generator.generate();

		// then
		Assertions.assertThat(nickname.length()).isEqualTo(expectedLength);
	}
}
