package co.fineants.api.domain.member.service;

import java.util.Optional;

public interface VerifyCodeRepository {
	Optional<String> get(String email);

	void save(String email, String verifyCode);
}
