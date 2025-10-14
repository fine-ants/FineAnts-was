package co.fineants.member.domain;

import java.util.Optional;

public interface MemberRepository {
	Optional<Member> findMemberByEmailAndProvider(MemberEmail email, String provider);

	Optional<Member> findMemberByNicknameAndNotMemberId(Nickname nickname, Long memberId);

	Optional<Member> findMemberByNickname(Nickname nickname);

	int modifyMemberPassword(String password, Long id);
}
