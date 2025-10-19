package co.fineants.member.application.listener;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import co.fineants.AbstractContainerBaseTest;
import co.fineants.TestDataFactory;
import co.fineants.api.domain.fcm.domain.entity.FcmToken;
import co.fineants.api.domain.fcm.repository.FcmRepository;
import co.fineants.member.domain.Member;
import co.fineants.member.domain.MemberRepository;
import co.fineants.member.domain.NotificationPreference;
import co.fineants.member.domain.event.NotificationPreferenceChangeEvent;
import co.fineants.role.domain.Role;
import co.fineants.role.domain.RoleRepository;

class NotificationPreferenceChangeEventListenerTest extends AbstractContainerBaseTest {

	@Autowired
	private NotificationPreferenceChangeEventListener listener;

	@Autowired
	private MemberRepository memberRepository;

	@Autowired
	private FcmRepository fcmRepository;

	@Autowired
	private RoleRepository roleRepository;
	private Member member;

	@BeforeEach
	void setUp() {
		Member newMember = TestDataFactory.createMember();
		NotificationPreference notificationPreference = NotificationPreference.create(
			false,
			false,
			false,
			false
		);
		newMember.setNotificationPreference(notificationPreference);
		Role userRole = roleRepository.findRoleByRoleName("ROLE_USER").orElseThrow();
		newMember.addRoleId(userRole.getId());
		this.member = memberRepository.save(newMember);

		setAuthentication(this.member);
	}

	@Test
	void on_whenNotificationPreferenceIsAllInActive_thenDeleteFcmToken() {
		FcmToken fcmToken = fcmRepository.save(FcmToken.create(member, "fcmToken"));
		NotificationPreferenceChangeEvent event = new NotificationPreferenceChangeEvent(
			member.getId(), fcmToken.getId());

		listener.on(event);

		Assertions.assertThat(fcmRepository.findById(fcmToken.getId())).isEmpty();
	}
}
