package co.fineants.api.domain.member.domain.factory;

import org.springframework.stereotype.Component;

import co.fineants.api.domain.member.domain.entity.MemberProfile;
import co.fineants.api.domain.member.domain.entity.Nickname;
import co.fineants.api.domain.member.service.factory.NicknameFactory;

@Component
public class MemberProfileFactory {

	private final NicknameFactory nicknameFactory;

	public MemberProfileFactory(NicknameFactory nicknameFactory) {
		this.nicknameFactory = nicknameFactory;
	}

	public MemberProfile localMemberProfile(String email, String nicknameValue, String encryptedPassword,
		String profileUrl) {
		Nickname nickname = nicknameFactory.create(nicknameValue);
		return MemberProfile.localMemberProfile(email, nickname, encryptedPassword, profileUrl);
	}
}
