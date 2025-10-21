package co.fineants.api.infra.mail;

import java.util.Map;

public interface MailHtmlRender {

	String render(Map<String, Object> variables);
}
