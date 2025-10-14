package co.fineants.api.domain.member.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import co.fineants.api.domain.member.domain.entity.Member;
import co.fineants.api.domain.member.domain.entity.MemberEmail;
import co.fineants.api.domain.member.domain.entity.Nickname;

public interface MemberRepository extends JpaRepository<Member, Long> {
	@Query("select distinct m from Member m "
		+ "where m.profile.email = :email and m.profile.provider = :provider")
	Optional<Member> findMemberByEmailAndProvider(@Param("email") MemberEmail email,
		@Param("provider") String provider);

	@Query("select m from Member m where m.profile.nickname = :nickname and m.id != :memberId")
	Optional<Member> findMemberByNicknameAndNotMemberId(@Param("nickname") Nickname nickname,
		@Param("memberId") Long memberId);

	@Query("select m from Member m where m.profile.nickname = :nickname")
	Optional<Member> findMemberByNickname(@Param("nickname") Nickname nickname);

	@Modifying
	@Query("update Member m set m.profile.password = :password where m.id = :id")
	int modifyMemberPassword(@Param("password") String password, @Param("id") Long id);
}
