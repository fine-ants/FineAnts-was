package co.fineants.api.domain.member.domain.entity;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import co.fineants.TestDataFactory;
import co.fineants.api.domain.notificationpreference.domain.entity.NotificationPreference;

class MemberTest {

	private Member createMember() {
		String email = "ants1234@gmail.com";
		String nickname = "ants1234";
		String provider = "local";
		String password = "ants1234@";
		String profileUrl = "profileUrl";
		MemberProfile memberProfile = new MemberProfile(email, nickname, provider, password, profileUrl);
		return new Member(memberProfile);
	}

	@DisplayName("회원에 매니저 역할을 추가한다")
	@Test
	void givenMember_whenAddMemberRole_thenAddRoleList() {
		// given
		Member member = TestDataFactory.createMember();
		Role userRole = Role.create("ROLE_USER", "회원");
		Role managerRole = Role.create("ROLE_MANAGER", "매니저");
		MemberRole memberMemberRole = MemberRole.of(member, userRole);
		MemberRole managerMemberRole = MemberRole.of(member, managerRole);
		// when
		member.addMemberRole(memberMemberRole, managerMemberRole);
		// then
		Assertions.assertThat(member.getRoles())
			.hasSize(2)
			.containsExactlyInAnyOrder(memberMemberRole, managerMemberRole);
	}

	@DisplayName("MemberRole을 다른 회원의 역할에 추가하면 기존 연관관계를 해제한다")
	@Test
	void addMemberRole_whenAssignMemberRoleToOtherMember_thenReleaseEntityRelationShip() {
		// given
		Member member = TestDataFactory.createMember();
		Role userRole = Role.create("ROLE_USER", "회원");
		Role managerRole = Role.create("ROLE_MANAGER", "매니저");
		MemberRole userMemberRole = MemberRole.of(member, userRole);
		MemberRole managerMemberRole = MemberRole.of(member, managerRole);
		member.addMemberRole(userMemberRole, managerMemberRole);

		Member otherMember = TestDataFactory.createMember("other1", "other1@gmail.com");
		otherMember.addMemberRole(MemberRole.of(otherMember, userRole));
		// when
		otherMember.addMemberRole(managerMemberRole);
		// then
		Assertions.assertThat(member.getRoles())
			.hasSize(1)
			.containsExactly(MemberRole.of(member, userRole));
		Assertions.assertThat(otherMember.getRoles())
			.hasSize(2)
			.containsExactlyInAnyOrder(MemberRole.of(otherMember, userRole), MemberRole.of(otherMember, managerRole));
	}

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
		Assertions.assertThat(preference.getMember()).isEqualTo(member);
	}

	@DisplayName("알림이 회원을 변경한다")
	@Test
	void givenNotificationPreference_whenSetMember_thenChangedMember() {
		// given
		Member member = TestDataFactory.createMember();
		NotificationPreference preference = NotificationPreference.allActive();
		// when
		preference.setMember(member);
		// then
		Assertions.assertThat(member.getNotificationPreference()).isEqualTo(preference);
		Assertions.assertThat(preference.getMember()).isEqualTo(member);
	}

	@DisplayName("회원의 간단한 정보를 출력한다")
	@Test
	void givenMember_whenToString_thenGenerateMemberInfo() {
		// given
		Member member = TestDataFactory.createMember();
		// when
		String actual = member.toString();
		// then
		String expected = "Member(id=null, nickname=nemo1234, email=dragonbead95@naver.com, roles=[])";
		Assertions.assertThat(actual).isEqualTo(expected);
	}

	@DisplayName("회원을 생성한다")
	@Test
	void canCreated() {
		Member member = createMember();

		Assertions.assertThat(member).isNotNull();
		Assertions.assertThat(member.getProfile()).isNotNull();
		Assertions.assertThat(member.getRoles()).isEmpty();
	}

	@DisplayName("회원에 역할을 추가한다")
	@Test
	void addMemberRole() {
		Member member = createMember();
		Role role = new Role("ROLE_USER", "회원");
		MemberRole memberRole = new MemberRole(member, role);

		member.addMemberRole(memberRole);

		Assertions.assertThat(member.hasRole("ROLE_USER")).isTrue();
	}
}
