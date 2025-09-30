package co.fineants.api.global.init;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import co.fineants.AbstractContainerBaseTest;
import co.fineants.api.domain.member.repository.MemberRepository;

class MemberSetupDataLoaderTest extends AbstractContainerBaseTest {

	@Autowired
	private MemberSetupDataLoader loader;

	@Autowired
	private MemberRepository memberRepository;

	@BeforeEach
	void setUp() {
		memberRepository.deleteAll();
	}

	@AfterEach
	void tearDown() {
		memberRepository.deleteAll();
	}

	@Test
	void setupMembers() {
		loader.setupMembers();

		Assertions.assertThat(memberRepository.findAll())
			.hasSize(1);
	}
}
