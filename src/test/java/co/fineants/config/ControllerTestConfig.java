package co.fineants.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;

import co.fineants.api.domain.fcm.service.FcmService;
import co.fineants.api.domain.member.service.MemberNotificationPreferenceService;
import co.fineants.api.domain.member.service.MemberNotificationService;
import co.fineants.api.domain.member.service.MemberService;
import co.fineants.api.domain.portfolio.service.PortFolioService;

@TestConfiguration
public class ControllerTestConfig {
	@MockBean
	private FcmService fcmService;

	@MockBean
	private MemberNotificationService notificationService;

	@MockBean
	private MemberNotificationPreferenceService preferenceService;

	@MockBean
	private MemberService memberService;

	@MockBean
	private PortFolioService portFolioService;
}
