package co.fineants.api.domain.member.service;

import java.util.Optional;

public interface TokenManagementService {
	void banRefreshToken(String token);

	void banAccessToken(String token);

	boolean isAlreadyLogout(String token);

	void clear();

	// TODO: vericy code와 token 도메인 개념을 분리
	Optional<String> get(String key);

	void saveEmailVerifCode(String email, String verifCode);
}
