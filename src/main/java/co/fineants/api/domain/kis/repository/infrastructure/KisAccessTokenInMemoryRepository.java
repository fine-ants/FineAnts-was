package co.fineants.api.domain.kis.repository.infrastructure;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import co.fineants.api.domain.kis.client.KisAccessToken;
import co.fineants.api.domain.kis.domain.repository.KisAccessTokenRepository;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Repository
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PUBLIC)
@ToString
public class KisAccessTokenInMemoryRepository implements KisAccessTokenRepository {

	private KisAccessToken accessToken;

	@Override
	public boolean isAccessTokenExpired(LocalDateTime dateTime) {
		if (accessToken == null) {
			return true;
		}
		return accessToken.isAccessTokenExpired(dateTime);
	}

	@Override
	public void refreshAccessToken(KisAccessToken accessToken) {
		this.accessToken = accessToken;
	}

	@Override
	public String createAuthorization() {
		if (accessToken == null) {
			return null;
		}
		return accessToken.createAuthorization();
	}

	@Override
	public boolean isTokenExpiringSoon(LocalDateTime localDateTime) {
		if (accessToken == null) {
			return true;
		}
		return accessToken.betweenSecondFrom(localDateTime).toSeconds() < 3600;
	}

	@Override
	public Optional<KisAccessToken> getAccessToken() {
		return Optional.ofNullable(accessToken);
	}
}

