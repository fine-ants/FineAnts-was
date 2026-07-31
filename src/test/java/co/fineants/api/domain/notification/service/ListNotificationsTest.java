package co.fineants.api.domain.notification.service;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import co.fineants.AbstractContainerBaseTest;
import co.fineants.TestDataFactory;
import co.fineants.api.domain.notification.repository.NotificationRepository;
import co.fineants.api.global.errors.exception.business.ForbiddenException;
import co.fineants.member.domain.Member;
import co.fineants.member.domain.MemberRepository;

class ListNotificationsTest extends AbstractContainerBaseTest {

	@Autowired
	private ListNotifications listNotifications;

	@Autowired
	private MemberRepository memberRepository;

	@Autowired
	private NotificationRepository notificationRepository;
	private Member member;

	@BeforeEach
	void setUp() {
		member = memberRepository.save(createMember());
		setAuthentication(member);
	}

	@DisplayName("사용자는 다른 사용자의 알림 메시지들을 조회할 수 없습니다.")
	@Test
	void byId_whenOtherMemberFetch_thenThrowException() {
		// given
		Member hacker = memberRepository.save(createMember("hacker"));
		notificationRepository.saveAll(TestDataFactory.createNotifications(member));

		setAuthentication(hacker);
		// when
		Throwable throwable = catchThrowable(() -> listNotifications.byId(member.getId()));

		// then
		assertThat(throwable)
			.isInstanceOf(ForbiddenException.class);
	}

}
