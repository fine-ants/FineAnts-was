package co.fineants.api.domain.kis.scheduler;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import co.fineants.TestDataFactory;
import co.fineants.api.domain.kis.client.KisAccessToken;
import co.fineants.api.domain.kis.client.KisClient;
import co.fineants.api.domain.kis.service.KisAccessTokenService;
import co.fineants.api.global.common.delay.DelayManager;
import co.fineants.api.global.common.time.LocalDateTimeService;
import reactor.core.publisher.Mono;

@ExtendWith(MockitoExtension.class)
class KisSchedulerTest {

	@InjectMocks
	private KisScheduler kisScheduler;

	@Mock
	private KisAccessTokenService kisAccessTokenService;

	@Mock
	private LocalDateTimeService localDateTimeService;

	@Spy
	private DelayManager delayManager;

	@Mock
	private KisClient kisClient;

	@DisplayName("액세스 토큰의 만료시간이 1시간 이전이어서 재발급하여 저장소에 저장된다")
	@Test
	void should_refresh_and_save_access_token_when_expiration_time_is_before_1_hour() {
		// given
		LocalDateTime baseTime = LocalDate.of(2026, 7, 24).atStartOfDay();
		KisAccessToken newAccessToken = TestDataFactory.createKisAccessToken(baseTime);
		BDDMockito.given(kisClient.fetchAccessToken())
			.willReturn(Mono.just(newAccessToken));

		BDDMockito.given(localDateTimeService.getLocalDateTimeWithNow())
			.willReturn(baseTime);
		BDDMockito.given(kisAccessTokenService.isAccessTokenExpiringSoon(baseTime))
			.willReturn(true);
		BDDMockito.given(delayManager.fixedAccessTokenDelay())
			.willReturn(Duration.ofMillis(10));
		// when
		kisScheduler.checkAndReissueAccessToken();
		// then
		BDDMockito.verify(kisAccessTokenService, Mockito.times(1))
			.saveAccessToken(newAccessToken, baseTime);
	}
}
