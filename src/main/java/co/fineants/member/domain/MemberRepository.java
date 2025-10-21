package co.fineants.member.domain;

import java.util.List;
import java.util.Optional;

public interface MemberRepository {
	Optional<Member> findMemberByEmailAndProvider(MemberEmail email, String provider);

	Optional<Member> findMemberByNicknameAndNotMemberId(Nickname nickname, Long memberId);

	Optional<Member> findMemberByNickname(Nickname nickname);

	Optional<Member> findById(Long memberId);

	List<Member> findAllById(List<Long> ids);

	List<Member> findAll();

	int modifyMemberPassword(String password, Long id);

	Member save(Member member);

	void deleteAll();

	void delete(Member member);
}
