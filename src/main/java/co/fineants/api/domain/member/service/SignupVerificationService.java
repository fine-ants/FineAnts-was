package co.fineants.api.domain.member.service;

import java.util.Map;

import org.springframework.stereotype.Service;

import co.fineants.api.domain.member.domain.factory.MimeMessageFactory;
import co.fineants.api.infra.mail.EmailService;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SignupVerificationService {

	private final VerifyCodeGenerator generator;
	private final VerifyCodeManagementService codeService;
	private final MimeMessageFactory verifyCodeMimeMessageFactory;
	private final EmailService emailService;

	public void sendSignupVerification(String email) {
		// 검증 코드 생성
		String verifyCode = generator.generate();
		// 검증 코드 임시 저장
		codeService.saveVerifyCode(email, verifyCode);
		// 이메일 메시지 생성
		Map<String, Object> variables = Map.of("verifyCode", verifyCode);
		MimeMessage message = verifyCodeMimeMessageFactory.create(email, variables);
		// 검증 코드 이메일 전송
		emailService.sendEmail(message);
	}
}
