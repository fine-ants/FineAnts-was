package codesquad.fineants.spring.api.member.service;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import codesquad.fineants.spring.AbstractContainerBaseTest;

class VerifyCodeGeneratorTest extends AbstractContainerBaseTest {

	@Autowired
	private VerifyCodeGenerator verifyCodeGenerator;

	@DisplayName("사용자는 검증코드를 생성한다")
	@Test
	void generate() {
		// given

		// when
		String result = verifyCodeGenerator.generate();

		// then
		assertThat(result.length()).isEqualTo(6);
	}
}
