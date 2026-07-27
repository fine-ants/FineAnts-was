package co.fineants.api.domain.kis.domain.repository;

import java.time.LocalDateTime;
import java.util.Optional;

import co.fineants.api.domain.kis.client.KisAccessToken;

public interface KisAccessTokenRepository {
	void save(KisAccessToken accessToken);

	void save(KisAccessToken accessToken, LocalDateTime now);

	Optional<KisAccessToken> get();

	boolean isAccessTokenExpired(LocalDateTime dateTime);

	String createAuthorization();

	boolean isTokenExpiringSoon(LocalDateTime localDateTime);

	void clear();
}
