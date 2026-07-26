package co.fineants.api.domain.kis.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
	public static final String ACCESS_TOKEN_MAP_KEY = "kis:accessTokenMap";
	public static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

	private final KisAccessTokenRepository repository;

	public Optional<KisAccessToken> getAccessTokenMap() {
		return repository.get();
	}

	public void setAccessTokenMap(KisAccessToken accessToken, LocalDateTime expiredDateTime) {
		repository.save(accessToken, expiredDateTime);
	}

	public void deleteAccessTokenMap() {
		repository.clear();
	}
}
