package co.fineants.api.domain.member.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;

import co.fineants.api.domain.member.domain.factory.ExchangeRateErrorMimeMessageFactory;
import co.fineants.api.domain.member.domain.factory.MemberProfileFactory;
import co.fineants.api.domain.member.domain.factory.VerifyCodeMimeMessageFactory;
import co.fineants.api.domain.member.service.ExchangeRateErrorMailHtmlRender;
import co.fineants.api.domain.member.service.VerifyCodeMailHtmlRender;

@Configuration
public class MemberFactoryConfig {

	@Bean
	public MemberProfileFactory memberProfileFactory() {
		return new MemberProfileFactory();
	}

	@Bean
	public VerifyCodeMimeMessageFactory verifyCodeMimeMessageFactory(VerifyCodeMailHtmlRender render,
		JavaMailSender sender) {
		String subject = "Finants 회원가입 인증 코드";
		return new VerifyCodeMimeMessageFactory(render, sender, subject);
	}

	@Bean
	public ExchangeRateErrorMimeMessageFactory exchangeRateErrorMimeMessageFactory(
		ExchangeRateErrorMailHtmlRender render,
		JavaMailSender sender) {
		String subject = "환율 API 서버 오류";
		return new ExchangeRateErrorMimeMessageFactory(render, sender, subject);
	}
}
