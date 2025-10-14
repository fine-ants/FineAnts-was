package co.fineants.api.domain.member.domain.factory;

import org.springframework.stereotype.Component;

import co.fineants.api.domain.member.domain.entity.MemberEmail;
import co.fineants.api.domain.member.domain.entity.MemberProfile;
import co.fineants.api.domain.member.domain.entity.Nickname;

@Component
public class MemberProfileFactory {

	public MemberProfile localMemberProfile(String email, String nickname, String encryptedPassword,
		String profileUrl) {
		MemberEmail memberEmail = new MemberEmail(email);
		Nickname memberNickname = new Nickname(nickname);
		return MemberProfile.localMemberProfile(memberEmail, memberNickname, encryptedPassword, profileUrl);
	}
}
