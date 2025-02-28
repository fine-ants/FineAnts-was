package co.fineants.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;

import co.fineants.api.domain.fcm.service.FcmService;

@TestConfiguration
public class ControllerTestConfig {
	@MockBean
	private FcmService fcmService;
}
