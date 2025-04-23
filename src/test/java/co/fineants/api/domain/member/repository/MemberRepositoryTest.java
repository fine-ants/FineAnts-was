package co.fineants.api.domain.member.repository;

import java.util.Optional;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import co.fineants.AbstractDataJpaBaseTest;
import co.fineants.TestDataFactory;
import co.fineants.api.domain.member.domain.entity.Member;

class MemberRepositoryTest extends AbstractDataJpaBaseTest {

	@Autowired
	private MemberRepository repository;

	@DisplayName("이메일과 provider과 동일한 회원이 있으면 true를 반환한다")
	@Test
	void findMemberByEmailAndProvider() {
		// given
		repository.save(TestDataFactory.createMember());
		String email = "dragonbead95@naver.com";
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
		String email = "dragonbead95@naver.com";
		String provider = "local";
		// when
		Optional<Member> actual = repository.findMemberByEmailAndProvider(email, provider);
		// then
		Assertions.assertThat(actual).isEmpty();
	}
}
