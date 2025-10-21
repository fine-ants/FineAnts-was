package co.fineants.member.application;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import co.fineants.member.application.VerifyCodeGenerator;

class VerifyCodeGeneratorTest {

	private VerifyCodeGenerator verifyCodeGenerator;

	@BeforeEach
	void setUp() {
		verifyCodeGenerator = new VerifyCodeGenerator(6, 1000000);
	}

	@DisplayName("사용자는 검증코드를 생성한다")
	@Test
	void generate() {
		// given
		// when
		String result = verifyCodeGenerator.generate();
		// then
		assertThat(result).hasSize(6);
	}
}
