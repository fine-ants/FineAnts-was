package co.fineants.api.domain.member.repository;

import java.util.Optional;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import co.fineants.AbstractContainerBaseTest;
import co.fineants.TestDataFactory;
import co.fineants.member.domain.Member;
import co.fineants.member.domain.MemberEmail;
import co.fineants.member.domain.MemberRepository;
import co.fineants.role.domain.Role;
import co.fineants.role.domain.RoleRepository;

@Transactional
class MemberRepositoryTest extends AbstractContainerBaseTest {

	@Autowired
	private MemberRepository repository;

	@Autowired
	private RoleRepository roleRepository;

	@DisplayName("이메일과 provider과 동일한 회원이 있으면 true를 반환한다")
	@Test
	void findMemberByEmailAndProvider() {
		// given
		repository.save(TestDataFactory.createMember());
		MemberEmail email = new MemberEmail("dragonbead95@naver.com");
		String provider = "local";
		// when
		Optional<Member> findMember = repository.findMemberByEmailAndProvider(email, provider);
		// then
		Assertions.assertThat(findMember).isPresent();
	}

	@DisplayName("이메일과 provider과 동일한 회원이 없으면 false를 반환한다")
	@Test
	void findMemberByEmailAndProviderWithMember() {
		// given
		MemberEmail email = new MemberEmail("dragonbead95@naver.com");
		String provider = "local";
		// when
		Optional<Member> actual = repository.findMemberByEmailAndProvider(email, provider);
		// then
		Assertions.assertThat(actual).isEmpty();
	}

	@DisplayName("회원을 여러번 저장해도 MemberRole 테이블 데이터가 중복되지 않아야 한다")
	@Test
	void saveMemberTwice() {
		// given
		Member member = TestDataFactory.createMember();
		Role roleUser = roleRepository.findRoleByRoleName("ROLE_USER").orElseThrow();
		member.addRoleId(roleUser.getId());
		Member saveMember = repository.save(member);
		// when
		repository.save(saveMember);
		// then
		Member findMember = repository.findById(saveMember.getId()).orElseThrow();
		Assertions.assertThat(findMember.getRoleIds()).hasSize(1);
	}
}
