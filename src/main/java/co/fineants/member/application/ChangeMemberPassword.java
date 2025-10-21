package co.fineants.member.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.fineants.api.global.errors.exception.business.MemberNotFoundException;
import co.fineants.api.global.errors.exception.business.PasswordConfirmInvalidInputException;
import co.fineants.api.global.errors.exception.business.PasswordInvalidInputException;
import co.fineants.member.domain.Member;
import co.fineants.member.domain.MemberPassword;
import co.fineants.member.domain.MemberPasswordEncoder;
import co.fineants.member.domain.MemberRepository;
import co.fineants.member.presentation.dto.request.PasswordUpdateRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChangeMemberPassword {

	private final MemberRepository memberRepository;
	private final MemberPasswordEncoder passwordEncoder;

	@Transactional
	public void changePassword(PasswordUpdateRequest request, Long memberId) {
		Member member = findMember(memberId);
		// 현재 비밀번호 일치 여부 확인
		String currentPassword = request.getCurrentPassword();
		String encodedMemberPassword = member.getPassword().orElse(null);
		if (!passwordEncoder.matches(currentPassword, encodedMemberPassword)) {
			throw new PasswordInvalidInputException(currentPassword);
		}

		// 새 비밀번호와 새 비밀번호 확인 일치 여부 확인
		String newPassword = request.getNewPassword();
		String newPasswordConfirm = request.getNewPasswordConfirm();
		if (!newPassword.equals(newPasswordConfirm)) {
			throw new PasswordConfirmInvalidInputException(newPassword, newPasswordConfirm);
		}
		MemberPassword memberPassword = new MemberPassword(newPassword, passwordEncoder);
		member.changePassword(memberPassword);
	}

	private Member findMember(Long id) {
		return memberRepository.findById(id)
			.orElseThrow(() -> new MemberNotFoundException(id.toString()));
	}
}
