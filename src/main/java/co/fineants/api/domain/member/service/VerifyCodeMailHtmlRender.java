package co.fineants.api.domain.member.service;

import java.util.Locale;
import java.util.Map;

import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

public class VerifyCodeMailHtmlRender implements MailHtmlRender {

	private final String subject;
	private final String templateName;
	private final SpringTemplateEngine engine;

	public VerifyCodeMailHtmlRender(String subject, String templateName, SpringTemplateEngine engine) {
		this.subject = subject;
		this.templateName = templateName;
		this.engine = engine;
	}

	@Override
	public String render(Map<String, Object> variables) {
		return engine.process(templateName, new Context(Locale.KOREA, variables));
	}
}
