package co.fineants.api.domain.kis.scheduler;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
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
import co.fineants.api.domain.kis.repository.FileHolidayRepository;
import co.fineants.api.domain.kis.repository.infrastructure.KisAccessTokenInMemoryRepository;
import co.fineants.api.domain.kis.service.KisAccessTokenRedisService;
import co.fineants.api.domain.kis.service.KisService;
import co.fineants.api.global.common.delay.DelayManager;
import co.fineants.api.global.common.time.LocalDateTimeService;
import reactor.core.publisher.Mono;

@ExtendWith(MockitoExtension.class)
class KisSchedulerTest {

	@InjectMocks
	private KisScheduler kisScheduler;

	@Mock
	private KisAccessTokenRedisService kisAccessTokenRedisService;

	@Mock
	private LocalDateTimeService localDateTimeService;

	@Spy
	private DelayManager delayManager;

	@Mock
	private KisClient kisClient;

	@Mock
	private KisService kisService;

	@Mock
	private FileHolidayRepository fileHolidayRepository;

	private KisAccessTokenInMemoryRepository kisAccessTokenInMemoryRepository;

	@BeforeEach
	void clean() {
		kisAccessTokenInMemoryRepository = new KisAccessTokenInMemoryRepository(null);
		kisAccessTokenInMemoryRepository.save(null);
		kisScheduler = new KisScheduler(kisAccessTokenInMemoryRepository, kisAccessTokenRedisService,
			localDateTimeService,
			delayManager, kisClient, kisService, fileHolidayRepository);
	}

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
		// when
		kisScheduler.checkAndReissueAccessToken();
		// then
		Assertions.assertThat(kisAccessTokenInMemoryRepository.getAccessToken()).isPresent();
		BDDMockito.verify(kisAccessTokenRedisService, Mockito.times(1))
			.setAccessTokenMap(newAccessToken, baseTime);
	}
}
