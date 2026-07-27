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
	public void save(KisAccessToken accessToken) {
		this.accessToken = accessToken;
	}

	@Override
	public void save(KisAccessToken accessToken, LocalDateTime now) {
		if (accessToken == null) {
			throw new IllegalArgumentException("accessToken is null object");
		}
		this.accessToken = accessToken;
	}

	@Override
	public Optional<KisAccessToken> get() {
		return Optional.ofNullable(accessToken);
	}

	@Override
	public String createAuthorization() {
		if (accessToken == null) {
			return null;
		}
		return accessToken.createAuthorization();
	}

	@Override
	public void clear() {
		this.accessToken = null;
	}
}

