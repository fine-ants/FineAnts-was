package co.fineants.member.application;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

import co.fineants.AbstractContainerBaseTest;
import co.fineants.TestDataFactory;
import co.fineants.api.global.errors.exception.business.PasswordInvalidInputException;
import co.fineants.member.domain.Member;
import co.fineants.member.domain.MemberRepository;
import co.fineants.member.presentation.dto.request.PasswordUpdateRequest;

class ChangeMemberPasswordTest extends AbstractContainerBaseTest {

	@Autowired
	private ChangeMemberPassword changeMemberPassword;

	@Autowired
	private MemberRepository memberRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@DisplayName("회원의 비밀번호를 변경한다")
	@Test
	void changePassword_whenPasswordValid_thenChangedMemberPassword() {
		Member member = memberRepository.save(TestDataFactory.createMember());
		String currentPassword = "nemo1234@";
		String newPassword = "nemo2345@";
		String newPasswordConfirm = "nemo2345@";
		PasswordUpdateRequest request = new PasswordUpdateRequest(
			currentPassword,
			newPassword,
			newPasswordConfirm
		);

		changeMemberPassword.changePassword(request, member.getId());

		Member findMember = memberRepository.findById(member.getId()).orElseThrow();
		boolean matches = passwordEncoder.matches(newPassword, findMember.getPassword().orElseThrow());
		Assertions.assertThat(matches).isTrue();
	}

	@DisplayName("현재 비밀번호가 일치하지 않아서 비밀번호 변경에 실패한다")
	@Test
	void changePassword_whenCurrentPasswordNotMatch_thenThrowException() {
		Member member = memberRepository.save(TestDataFactory.createMember());
		String currentPassword = "xxx";
		String newPassword = "nemo2345@";
		String newPasswordConfirm = "nemo2345@";
		PasswordUpdateRequest request = new PasswordUpdateRequest(
			currentPassword,
			newPassword,
			newPasswordConfirm
		);

		Throwable throwable = Assertions.catchThrowable(
			() -> changeMemberPassword.changePassword(request, member.getId()));

		Assertions.assertThat(throwable)
			.isInstanceOf(PasswordInvalidInputException.class)
			.hasMessage(currentPassword);
	}

	@DisplayName("새로운 비밀번호와 새로운 비밀번호 확인이 일치하지 않으면 비밀번호 변경에 실패한다")
	@Test
	void changePassword_whenNewPasswordNotMatch_thenThrowException() {
		Member member = memberRepository.save(TestDataFactory.createMember());
		String currentPassword = "nemo1234@";
		String newPassword = "nemo2345@";
		String newPasswordConfirm = "nemo2345@@";
		PasswordUpdateRequest request = new PasswordUpdateRequest(
			currentPassword,
			newPassword,
			newPasswordConfirm
		);

		Throwable throwable = Assertions.catchThrowable(
			() -> changeMemberPassword.changePassword(request, member.getId()));

		Assertions.assertThat(throwable)
			.isInstanceOf(PasswordInvalidInputException.class)
			.hasMessage(newPassword);
	}
}
