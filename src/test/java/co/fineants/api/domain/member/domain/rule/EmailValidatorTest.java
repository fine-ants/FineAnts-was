package co.fineants.api.domain.member.domain.rule;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import co.fineants.AbstractContainerBaseTest;
import co.fineants.api.domain.member.domain.entity.Member;
import co.fineants.api.domain.member.repository.MemberRepository;
import co.fineants.api.domain.validator.member.EmailValidator;
import co.fineants.api.global.errors.exception.business.EmailDuplicateException;

class EmailValidatorTest extends AbstractContainerBaseTest {

	@Autowired
	private EmailValidator validator;

	@Autowired
	private MemberRepository memberRepository;

	@DisplayName("이메일이 검증을 통과하면 예외가 발생하지 않는다")
	@Test
	void givenEmail_whenValidateEmail_thenNotThrowException() {
		// given
		String email = "dragonbead95@naver.com";
		// when & then
		assertDoesNotThrow(() -> validator.validate(email));
	}

	@DisplayName("중복된 이메일이 주어지고 검증을 시도하면 예외가 발생한다")
	@Test
	void givenDuplicateEmail_whenValidateEmail_thenThrowException() {
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
