package co.fineants.api.infra.mail.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.thymeleaf.spring6.SpringTemplateEngine;

import co.fineants.api.domain.member.service.ExchangeRateErrorMailHtmlRender;
import co.fineants.api.domain.member.service.VerifyCodeMailHtmlRender;
import co.fineants.api.infra.mail.ExchangeRateErrorMimeMessageFactory;
import co.fineants.api.infra.mail.VerifyCodeMimeMessageFactory;

@Configuration
public class MailConfig {
	@Bean
	public VerifyCodeMailHtmlRender mailHtmlRender(
		@Value("${mail.templates.path.verify-code}") String templateName,
		SpringTemplateEngine engine) {
		return new VerifyCodeMailHtmlRender(templateName, engine);
	}

	@Bean
	public ExchangeRateErrorMailHtmlRender exchangeRateErrorMailHtmlRender(
		@Value("${mail.templates.path.exchange-rate-error}") String templateName,
		SpringTemplateEngine engine) {
		return new ExchangeRateErrorMailHtmlRender(templateName, engine);
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
