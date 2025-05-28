package co.fineants.api.domain.member.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.thymeleaf.spring6.SpringTemplateEngine;

import co.fineants.api.domain.member.service.ExchangeRateErrorMailHtmlRender;
import co.fineants.api.domain.member.service.VerifyCodeMailHtmlRender;

@Configuration
public class MailConfig {
	@Bean
	public VerifyCodeMailHtmlRender mailHtmlRender(SpringTemplateEngine engine) {
		String templateName = "mail-templates/verify-email_template";
		return new VerifyCodeMailHtmlRender(templateName, engine);
	}

	@Bean
	public ExchangeRateErrorMailHtmlRender exchangeRateErrorMailHtmlRender(SpringTemplateEngine engine) {
		String templateName = "mail-templates/exchange-rate-fail-notification_template";
		return new ExchangeRateErrorMailHtmlRender(templateName, engine);
	}
}
