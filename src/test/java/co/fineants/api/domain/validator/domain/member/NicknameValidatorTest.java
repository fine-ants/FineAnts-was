package co.fineants.api.domain.validator.domain.member;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import co.fineants.AbstractContainerBaseTest;
import co.fineants.api.global.errors.exception.business.NicknameDuplicateException;
import co.fineants.api.global.errors.exception.business.NicknameInvalidInputException;
import co.fineants.member.domain.MemberRepository;

class NicknameValidatorTest extends AbstractContainerBaseTest {

	@Autowired
	private NicknameValidator validator;

	@Autowired
	private MemberRepository memberRepository;

	@DisplayName("사용자는 닉네임이 중복되었는지 체크한다")
	@Test
	void givenNickname_whenValidateNickname_thenNotThrowException() {
		// given
		String nickname = "일개미1234";
		// when & then
		assertDoesNotThrow(() -> validator.validate(nickname));
	}

	@DisplayName("유효하지 않은 형식의 닉네임이 주어지고 검증을 시도하면 예외가 발생한다")
	@Test
	void givenInvalidNickname_whenValidateNickname_thenThrowException() {
		// given
		String nickname = "일";
		// when & then
		Throwable throwable = catchThrowable(() -> validator.validate(nickname));
		assertThat(throwable)
			.isInstanceOf(NicknameInvalidInputException.class)
			.hasMessage(nickname);
	}

	@DisplayName("중복된 닉네임이 주어지고 검증을 시도하면 예외가 발생한다")
	@Test
	void givenDuplicateNickname_whenValidateNickname_thenThrowException() {
		// given
		memberRepository.save(createMember("일개미1234"));
		String nickname = "일개미1234";

		// when
		Throwable throwable = catchThrowable(() -> validator.validate(nickname));

		// then
		assertThat(throwable)
			.isInstanceOf(NicknameDuplicateException.class)
			.hasMessage(nickname);
	}
}
