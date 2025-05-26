package co.fineants.api.domain.member.domain.rule;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import co.fineants.AbstractContainerBaseTest;
import co.fineants.api.domain.member.domain.entity.Member;
import co.fineants.api.domain.member.repository.MemberRepository;
import co.fineants.api.global.errors.exception.business.EmailDuplicateException;

class EmailValidatorTest extends AbstractContainerBaseTest {

	@Autowired
	private EmailValidator validator;

	@Autowired
	private MemberRepository memberRepository;

	@DisplayName("사용자는 이메일이 중복되었는지 검사한다")
	@Test
	void checkEmail() {
		// given
		String email = "dragonbead95@naver.com";
		// when & then
		assertDoesNotThrow(() -> validator.validate(email));
	}

	@DisplayName("사용자는 이메일 중복 검사 요청시 로컬 이메일이 존재하여 예외가 발생한다")
	@Test
	void checkEmail_whenDuplicatedLocalEmail_thenThrowBadRequestException() {
		// given
		Member member = memberRepository.save(createMember());
		String email = member.getEmail();

		// when
		Throwable throwable = catchThrowable(() -> validator.validate(email));

		// then
		assertThat(throwable)
			.isInstanceOf(EmailDuplicateException.class)
			.hasMessage(email);
	}
}
