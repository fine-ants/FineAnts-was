package co.fineants.api.domain.member.domain.entity;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import co.fineants.TestDataFactory;
import co.fineants.api.domain.notificationpreference.domain.entity.NotificationPreference;

class MemberTest {

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

	// @DisplayName("알림이 회원을 변경한다")
	// @Test
	// void setMember() {
	// 	// given
	// 	Member member = createMember();
	// 	NotificationPreference preference = NotificationPreference.create(false, false, false, false);
	// 	// when
	// 	preference.setMember(member);
	// 	// then
	// 	Assertions.assertThat(member.getNotificationPreference()).isEqualTo(preference);
	// 	Assertions.assertThat(preference.getMember()).isEqualTo(member);
	// }
	//
	// @DisplayName("회원의 간단한 정보를 출력한다")
	// @Test
	// void givenMember_whenToString_thenGenerateMemberInfo() {
	// 	// given
	// 	Member member = createMember();
	// 	// when
	// 	String actual = member.toString();
	// 	// then
	// 	Assertions.assertThat(actual)
	// 		.isEqualTo("Member(id=null, nickname=nemo1234, email=dragonbead95@naver.com, roles=[ROLE_USER])");
	// }
}
