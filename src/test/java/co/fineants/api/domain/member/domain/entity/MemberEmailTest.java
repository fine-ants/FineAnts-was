package co.fineants.api.domain.member.domain.entity;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class MemberEmailTest {
	@Test
	void canCreated() {
		MemberEmail memberEmail = new MemberEmail("ant1234@gmail.com");

		Assertions.assertThat(memberEmail).isNotNull();
	}
}
