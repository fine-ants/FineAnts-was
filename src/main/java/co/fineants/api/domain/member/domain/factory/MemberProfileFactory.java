package co.fineants.api.domain.member.domain.factory;

import co.fineants.api.domain.member.domain.entity.MemberProfile;

public class MemberProfileFactory {
	public MemberProfile localMemberProfile(String email, String nickname, String encryptedPassword,
		String profileUrl) {
		return MemberProfile.localMemberProfile(email, nickname, encryptedPassword, profileUrl);
	}
}
