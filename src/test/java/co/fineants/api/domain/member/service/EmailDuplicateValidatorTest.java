package co.fineants.api.domain.member.service;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import co.fineants.AbstractContainerBaseTest;
import co.fineants.TestDataFactory;
import co.fineants.member.domain.MemberEmail;
import co.fineants.member.infrastructure.MemberSpringDataJpaRepository;

class EmailDuplicateValidatorTest extends AbstractContainerBaseTest {

	@Autowired
	private EmailDuplicateValidator validator;

	@Autowired
	private MemberSpringDataJpaRepository repository;

	@AfterEach
	void tearDown() {
		repository.deleteAll();
	}

	@DisplayName("로컬 회원의 이메일이 중복되어 true를 반환한다")
	@Test
	void hasMemberWith_whenExistMember_thenReturnTrue() {
		repository.save(TestDataFactory.createMember());
		String provider = "local";
		MemberEmail email = new MemberEmail("dragonbead95@naver.com");

		boolean actual = validator.hasMemberWith(email, provider);

		Assertions.assertThat(actual).isTrue();
	}

	@DisplayName("로컬 회원의 이메일이 중복되지 않아서 false를 반환한다")
	@Test
	void hasMemberWith_whenNotExistMember_thenReturnFalse() {
		String provider = "local";
		MemberEmail email = new MemberEmail("dragonbead95@naver.com");

		boolean actual = validator.hasMemberWith(email, provider);

		Assertions.assertThat(actual).isFalse();
	}
}
