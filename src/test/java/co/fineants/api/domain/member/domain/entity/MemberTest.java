package co.fineants.api.domain.member.domain.entity;

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

	@DisplayName("회원의 간단한 정보를 출력한다")
	@Test
	void givenMember_whenToString_thenGenerateMemberInfo() {
		// given
		Member member = TestDataFactory.createMember();
		// when
		String actual = member.toString();
		// then
		String expected = "Member(id=null, nickname=nemo1234, email=dragonbead95@naver.com, roleIds=[])";
		Assertions.assertThat(actual).isEqualTo(expected);
	}

	@DisplayName("회원을 생성한다")
	@Test
	void canCreated() {
		Member member = createMember();

		Assertions.assertThat(member).isNotNull();
		Assertions.assertThat(member.getProfile()).isNotNull();
	}

	private Member createMember() {
		String email = "ants1234@gmail.com";
		String nickname = "ants1234";
		String provider = "local";
		String password = "ants1234@";
		String profileUrl = "profileUrl";
		MemberProfile memberProfile = new MemberProfile(email, nickname, provider, password, profileUrl);
		return new Member(memberProfile);
	}

	@DisplayName("회원에 Role 식별자값을 추가한다")
	@Test
	void addRoleId() {
		Member member = createMember();
		Long roleId = 1L;

		member.addRoleId(roleId);

		Assertions.assertThat(member.containsRoleId(roleId)).isTrue();
	}

	@DisplayName("회원에 Role 식별자값을 제거한다")
	@Test
	void removeRoleId() {
		Member member = createMember();
		Long roleId = 1L;
		member.addRoleId(roleId);

		member.removeRoleId(roleId);

		Assertions.assertThat(member.containsRoleId(roleId)).isFalse();
	}
}
