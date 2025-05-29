package co.fineants.api.domain.member.service;

import java.util.Optional;

public interface VerifyCodeRepository {
	Optional<String> getVerificationCode(String email);

	void saveVerifyCode(String email, String verifyCode);
}
