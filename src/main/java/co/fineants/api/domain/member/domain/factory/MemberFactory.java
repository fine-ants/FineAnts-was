package co.fineants.api.domain.member.domain.factory;

import co.fineants.api.domain.member.domain.entity.Member;
import co.fineants.api.domain.member.domain.entity.MemberProfile;

public class MemberFactory {
	public Member localMember(MemberProfile profile) {
		return Member.localMember(profile);
	}
}
