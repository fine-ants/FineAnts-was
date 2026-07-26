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
