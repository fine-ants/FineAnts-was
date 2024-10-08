package co.fineants.api.domain.kis.repository;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.stereotype.Component;

import co.fineants.api.domain.kis.client.KisAccessToken;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PUBLIC)
public class KisAccessTokenRepository {

	private KisAccessToken accessToken;

	public boolean isAccessTokenExpired(LocalDateTime dateTime) {
		if (accessToken == null) {
			return true;
		}
		return accessToken.isAccessTokenExpired(dateTime);
	}

	public void refreshAccessToken(KisAccessToken accessToken) {
		this.accessToken = accessToken;
	}

	public String createAuthorization() {
		if (accessToken == null) {
			return null;
		}
		return accessToken.createAuthorization();
	}

	public boolean isTokenExpiringSoon(LocalDateTime localDateTime) {
		if (accessToken == null) {
			return true;
		}
		return accessToken.betweenSecondFrom(localDateTime).toSeconds() < 3600;
	}

	public Optional<KisAccessToken> getAccessToken() {
		return Optional.ofNullable(accessToken);
	}
}

