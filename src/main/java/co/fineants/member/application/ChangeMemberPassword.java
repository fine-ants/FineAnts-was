package co.fineants.member.application;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.fineants.api.global.errors.exception.business.MemberNotFoundException;
import co.fineants.api.global.errors.exception.business.PasswordInvalidInputException;
import co.fineants.member.domain.Member;
import co.fineants.member.domain.MemberRepository;
import co.fineants.member.presentation.dto.request.PasswordModifyRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChangeMemberPassword {

	private final PasswordEncoder passwordEncoder;
	private final MemberRepository memberRepository;

	@Transactional
	public void changePassword(PasswordModifyRequest request, Long memberId) {
		Member member = findMember(memberId);
		if (!passwordEncoder.matches(request.currentPassword(), member.getPassword().orElse(null))) {
			throw new PasswordInvalidInputException(request.currentPassword());
		}
		if (!request.matchPassword()) {
			throw new PasswordInvalidInputException(request.currentPassword());
		}
		String newPassword = passwordEncoder.encode(request.newPassword());
		int count = memberRepository.modifyMemberPassword(newPassword, member.getId());
		log.info("member password change result : {}", count);
	}

	private Member findMember(Long id) {
		return memberRepository.findById(id)
			.orElseThrow(() -> new MemberNotFoundException(id.toString()));
	}
}
