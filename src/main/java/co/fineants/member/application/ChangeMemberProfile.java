package co.fineants.member.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import co.fineants.api.global.errors.exception.business.MemberNotFoundException;
import co.fineants.api.global.errors.exception.business.MemberProfileNotChangeException;
import co.fineants.api.global.errors.exception.business.NicknameDuplicateException;
import co.fineants.api.infra.s3.service.DeleteProfileImageFileService;
import co.fineants.api.infra.s3.service.WriteProfileImageFileService;
import co.fineants.member.domain.Member;
import co.fineants.member.domain.MemberRepository;
import co.fineants.member.domain.Nickname;
import co.fineants.member.presentation.dto.request.ProfileChangeServiceRequest;
import co.fineants.member.presentation.dto.response.ProfileChangeResponse;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ChangeMemberProfile {

	private final MemberRepository memberRepository;
	private final DeleteProfileImageFileService deleteProfileImageFileService;
	private final WriteProfileImageFileService writeProfileImageFileService;

	@Transactional
	public ProfileChangeResponse changeProfile(ProfileChangeServiceRequest request) {
		Member member = findMember(request.memberId());
		MultipartFile profileImageFile = request.getProfileImageFile();
		String profileUrl = null;

		// 변경할 정보가 없는 경우
		if (profileImageFile == null && !request.hasNickname()) {
			throw new MemberProfileNotChangeException(request.toString());
		}

		// 기존 프로필 파일 유지
		if (profileImageFile == null) {
			profileUrl = member.getProfileUrl().orElse(null);
		} else if (profileImageFile.isEmpty()) { // 기본 프로필 파일로 변경인 경우
			// 회원의 기존 프로필 사진 제거
			// 기존 프로필 파일 삭제
			member.getProfileUrl().ifPresent(deleteProfileImageFileService::delete);
		} else if (!profileImageFile.isEmpty()) { // 새로운 프로필 파일로 변경인 경우
			// 기존 프로필 파일 삭제
			member.getProfileUrl().ifPresent(deleteProfileImageFileService::delete);

			// 새로운 프로필 파일 저장
			profileUrl = writeProfileImageFileService.upload(profileImageFile);
		}
		member.changeProfileUrl(profileUrl);

		if (request.hasNickname()) {
			Nickname nickname = new Nickname(request.nickname());
			verifyNickname(nickname, member.getId());
			member.changeNickname(nickname);
		}
		return ProfileChangeResponse.from(member);
	}

	private Member findMember(Long memberId) {
		return memberRepository.findById(memberId)
			.orElseThrow(() -> new MemberNotFoundException(memberId.toString()));
	}

	private void verifyNickname(Nickname nickname, Long memberId) throws NicknameDuplicateException {
		if (memberRepository.findMemberByNicknameAndNotMemberId(nickname, memberId).isPresent()) {
			throw new NicknameDuplicateException(nickname.getValue());
		}
	}
}
