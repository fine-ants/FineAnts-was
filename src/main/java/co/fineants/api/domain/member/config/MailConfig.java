package co.fineants.api.domain.member.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.thymeleaf.spring6.SpringTemplateEngine;

import co.fineants.api.domain.member.service.VerifyCodeMailHtmlRender;

@Configuration
public class MailConfig {
	@Bean
	public VerifyCodeMailHtmlRender mailHtmlRender(SpringTemplateEngine engine) {
		String templateName = "mail-templates/verify-email_template";
		return new VerifyCodeMailHtmlRender(templateName, engine);
	}
}
