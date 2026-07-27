package co.fineants.api.domain.kis.service;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.stereotype.Service;

import co.fineants.api.domain.kis.client.KisAccessToken;
import co.fineants.api.domain.kis.domain.repository.KisAccessTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service
public class KisAccessTokenService {

	private final KisAccessTokenRepository repository;

	public void saveAccessToken(KisAccessToken accessToken, LocalDateTime now) {
		if (accessToken.isAccessTokenExpired(now)) {
			return;
		}
		repository.save(accessToken, now);
	}

	public Optional<KisAccessToken> getAccessToken() {
		return repository.get();
	}

	public boolean isAccessTokenExpired(LocalDateTime dateTime) {
		if (dateTime == null) {
			throw new IllegalArgumentException("dateTime must not null");
		}
		return repository.get()
			.map(token -> token.isAccessTokenExpired(dateTime))
			.orElse(true);
	}

	public String getAuthorization() {
		return repository.get()
			.map(KisAccessToken::createAuthorization)
			.orElseThrow(() -> new IllegalStateException("can get Authorization"));
	}

	public void deleteAccessTokenMap() {
		repository.clear();
	}
}
