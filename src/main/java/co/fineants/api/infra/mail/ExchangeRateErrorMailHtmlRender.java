package co.fineants.api.infra.mail;

import java.util.Locale;
import java.util.Map;

import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

public class ExchangeRateErrorMailHtmlRender implements MailHtmlRender {

	private final String templateName;
	private final SpringTemplateEngine engine;

	public ExchangeRateErrorMailHtmlRender(String templateName, SpringTemplateEngine engine) {
		this.templateName = templateName;
		this.engine = engine;
	}

	@Override
	public String render(Map<String, Object> variables) {
		return engine.process(templateName, new Context(Locale.KOREA, variables));
	}
}
