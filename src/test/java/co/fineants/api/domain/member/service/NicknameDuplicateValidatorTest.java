package co.fineants.api.domain.member.service;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import co.fineants.AbstractContainerBaseTest;
import co.fineants.TestDataFactory;
import co.fineants.api.domain.member.domain.entity.Nickname;
import co.fineants.api.domain.member.repository.MemberRepository;
import co.fineants.api.domain.member.service.factory.NicknameFactory;

class NicknameDuplicateValidatorTest extends AbstractContainerBaseTest {

	@Autowired
	private NicknameDuplicateValidator validator;

	@Autowired
	private MemberRepository memberRepository;

	@Autowired
	private NicknameFactory factory;

	@AfterEach
	void tearDown() {
		memberRepository.deleteAll();
	}

	@Test
	void isDuplicate_whenDuplicatedNickname_thenReturnTrue() {
		memberRepository.save(TestDataFactory.createMember());
		Nickname nickname = factory.create("nemo1234");

		boolean actual = validator.isDuplicate(nickname);

		Assertions.assertThat(actual).isTrue();
	}

	@Test
	void isDuplicate_whenNotDuplicatedNickname_thenReturnFalse() {
		Nickname nickname = factory.create("nemo2345");

		boolean actual = validator.isDuplicate(nickname);

		Assertions.assertThat(actual).isFalse();
	}
}
