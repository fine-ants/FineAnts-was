package co.fineants.member.infrastructure;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import co.fineants.AbstractContainerBaseTest;
import co.fineants.TestDataFactory;
import co.fineants.member.domain.Member;
import co.fineants.member.domain.MemberRepository;

class MemberJpaRepositoryTest extends AbstractContainerBaseTest {

	@Autowired
	private MemberRepository memberRepository;

	@Test
	void saveMember() {
		Member member = TestDataFactory.createOauthMember();

		Member saveMember = memberRepository.save(member);

		Assertions.assertThat(saveMember.getId()).isNotNull();
	}
}
