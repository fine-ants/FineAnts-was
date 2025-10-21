package co.fineants.member.application;

import org.springframework.stereotype.Service;

import co.fineants.api.global.errors.exception.business.VerifyCodeInvalidInputException;
import co.fineants.member.domain.VerifyCodeRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class VerifyCode {

	private final VerifyCodeRepository repository;

	public void verifyBy(String email, String code) {
		String verifyCode = repository.get(email)
			.orElseThrow(() -> new VerifyCodeInvalidInputException(code));
		if (!verifyCode.equals(code)) {
			throw new VerifyCodeInvalidInputException(code);
		}
	}
}
