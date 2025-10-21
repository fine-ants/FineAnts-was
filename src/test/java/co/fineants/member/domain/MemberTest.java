package co.fineants.member.domain;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import co.fineants.TestDataFactory;

class MemberTest {

	@DisplayName("회원의 알림 설정을 전부 활성화로 변경한다")
	@Test
	void givenMember_whenSetNotificationPreference_thenNotificationPreferenceIsAllActive() {
		// given
		Member member = TestDataFactory.createMember();
		NotificationPreference preference = NotificationPreference.allActive();
		// when
		member.setNotificationPreference(preference);
		// then
		NotificationPreference expected = NotificationPreference.allActive();
		Assertions.assertThat(member.getNotificationPreference()).isEqualTo(expected);
	}
	
	@DisplayName("회원을 생성한다")
	@Test
	void canCreated() {
		Member member = TestDataFactory.createMember();

		Assertions.assertThat(member).isNotNull();
		Assertions.assertThat(member.getProfile()).isNotNull();
	}

	@DisplayName("회원에 Role 식별자값을 추가한다")
	@Test
	void addRoleId() {
		Member member = TestDataFactory.createMember();
		Long roleId = 1L;

		member.addRoleId(roleId);

		Assertions.assertThat(member.containsRoleId(roleId)).isTrue();
	}

	@DisplayName("회원에 Role 식별자값을 제거한다")
	@Test
	void removeRoleId() {
		Member member = TestDataFactory.createMember();
		Long roleId = 1L;
		member.addRoleId(roleId);

		member.removeRoleId(roleId);

		Assertions.assertThat(member.containsRoleId(roleId)).isFalse();
	}
}
