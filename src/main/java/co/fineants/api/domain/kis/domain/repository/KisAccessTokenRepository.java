package co.fineants.api.domain.kis.domain.repository;

import java.time.LocalDateTime;
import java.util.Optional;

import co.fineants.api.domain.kis.client.KisAccessToken;

public interface KisAccessTokenRepository {
	boolean isAccessTokenExpired(LocalDateTime dateTime);

	void refreshAccessToken(KisAccessToken accessToken);

	String createAuthorization();

	boolean isTokenExpiringSoon(LocalDateTime localDateTime);

	Optional<KisAccessToken> getAccessToken();
}
