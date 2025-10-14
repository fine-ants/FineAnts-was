package co.fineants.api.infra.mail.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.thymeleaf.spring6.SpringTemplateEngine;

import co.fineants.api.domain.member.service.ExchangeRateErrorMailHtmlRender;
import co.fineants.api.domain.member.service.VerifyCodeMailHtmlRender;

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
}
