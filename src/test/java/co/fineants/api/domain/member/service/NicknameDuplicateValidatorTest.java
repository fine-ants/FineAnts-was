package co.fineants.api.domain.member.service;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import co.fineants.AbstractContainerBaseTest;
import co.fineants.TestDataFactory;
import co.fineants.member.domain.Nickname;
import co.fineants.api.domain.member.repository.MemberRepository;

class NicknameDuplicateValidatorTest extends AbstractContainerBaseTest {

	@Autowired
	private NicknameDuplicateValidator validator;

	@Autowired
	private MemberRepository memberRepository;

	@AfterEach
	void tearDown() {
		memberRepository.deleteAll();
	}

	@DisplayName("중복된 닉네임은 true를 반환한다.")
	@Test
	void isDuplicate_whenDuplicatedNickname_thenReturnTrue() {
		memberRepository.save(TestDataFactory.createMember());
		Nickname nickname = new Nickname("nemo1234");

		boolean actual = validator.isDuplicate(nickname);

		Assertions.assertThat(actual).isTrue();
	}

	@DisplayName("중복되지 않은 닉네임은 false를 반환한다.")
	@Test
	void isDuplicate_whenNotDuplicatedNickname_thenReturnFalse() {
		Nickname nickname = new Nickname("nemo2345");

		boolean actual = validator.isDuplicate(nickname);

		Assertions.assertThat(actual).isFalse();
	}

	@DisplayName("닉네임은 영문 대소문자를 구분하지 않는다")
	@Test
	void isDuplicate_whenUppercaseNickname_thenReturnFalse() {
		memberRepository.save(TestDataFactory.createMember());
		Nickname nickname = new Nickname("Nemo1234");

		boolean actual = validator.isDuplicate(nickname);

		Assertions.assertThat(actual).isTrue();
	}
}
