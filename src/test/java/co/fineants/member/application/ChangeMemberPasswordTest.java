package co.fineants.member.application;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

import co.fineants.AbstractContainerBaseTest;
import co.fineants.TestDataFactory;
import co.fineants.member.domain.Member;
import co.fineants.member.domain.MemberRepository;
import co.fineants.member.presentation.dto.request.PasswordModifyRequest;

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
		PasswordModifyRequest request = new PasswordModifyRequest(
			currentPassword,
			newPassword,
			newPasswordConfirm
		);

		changeMemberPassword.changePassword(request, member.getId());

		Member findMember = memberRepository.findById(member.getId()).orElseThrow();
		boolean matches = passwordEncoder.matches(newPassword, findMember.getPassword().orElseThrow());
		Assertions.assertThat(matches).isTrue();
	}
}
