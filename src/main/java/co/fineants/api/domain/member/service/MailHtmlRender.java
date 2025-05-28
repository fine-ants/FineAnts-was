package co.fineants.api.domain.member.service;

import java.util.Map;

public interface MailHtmlRender {

	String render(Map<String, Object> variables);
}
