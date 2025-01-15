package co.fineants.api.domain.member.service;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class NicknameGeneratorTest {

	private NicknameGenerator generator;

	@BeforeEach
	void setUp() {
		generator = new NicknameGenerator("일개미", 7);
	}

	@DisplayName("회원의 랜덤 닉네임을 생성합니다")
	@Test
	void generate() {
		// given
		int expectedLength = 10;
		// when
		String nickname = generator.generate();
		// then
		Assertions.assertThat(nickname).hasSize(expectedLength);
	}
}
