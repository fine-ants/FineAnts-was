package co.fineants.api.domain.kis.service;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import co.fineants.AbstractContainerBaseTest;
import co.fineants.api.domain.kis.client.KisAccessToken;

class KisAccessTokenRedisServiceTest extends AbstractContainerBaseTest {

	@Autowired
	private KisAccessTokenRedisService service;
	
	@DisplayName("kis 액세스 토큰맵을 저장한다")
	@Test
	void setAccessTokenMap() {
		// given
		KisAccessToken kisAccessToken = createKisAccessToken();

		// when
		service.setAccessTokenMap(kisAccessToken, createNow());

		// then
		assertThat(service.getAccessTokenMap()).isPresent();
	}

	@DisplayName("이미 만료된 액세스 토큰을 redis에 저장할 수 없다.")
	@Test
	void setAccessTokenMapWithExpiredAccessToken() {
		// given
		KisAccessToken accessToken = createKisAccessToken();

		// when
		service.setAccessTokenMap(accessToken, LocalDateTime.of(2023, 12, 8, 15, 0, 0));

		// then
		assertThat(service.getAccessTokenMap()).isEmpty();
	}

	@DisplayName("kis 액세스 토큰맵을 가져온다")
	@Test
	void getAccessTokenMap() {
		// given
		service.setAccessTokenMap(createKisAccessToken(), createNow());

		// when
		KisAccessToken accessToken = service.getAccessTokenMap().orElseThrow();

		// then
		assertThat(accessToken)
			.extracting("accessToken", "tokenType", "accessTokenExpired", "expiresIn")
			.containsExactlyInAnyOrder(
				"accessToken",
				"Bearer",
				LocalDateTime.of(2023, 12, 7, 11, 41, 27),
				86400
			);
	}

	@DisplayName("redis에 accessToken이 없는 경우 Optional.empty()를 반환한다")
	@Test
	void getAccessTokenMap_whenEmptyAccessToken_thenReturnEmptyOptional() {
		// given
		service.deleteAccessTokenMap();

		// when
		Optional<KisAccessToken> optionalKisAccessToken = service.getAccessTokenMap();

		// then
		assertThat(optionalKisAccessToken).isEmpty();
	}

	private LocalDateTime createNow() {
		return LocalDateTime.of(2023, 12, 6, 14, 0, 0);
	}

	public KisAccessToken createKisAccessToken() {
		return new KisAccessToken(
			"accessToken",
			"Bearer",
			LocalDateTime.of(2023, 12, 7, 11, 41, 27),
			86400
		);
	}
}
