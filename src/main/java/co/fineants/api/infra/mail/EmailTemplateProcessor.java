package co.fineants.api.infra.mail;

import java.io.IOException;
import java.util.Map;

import org.springframework.core.io.ClassPathResource;

public class EmailTemplateProcessor {
	public String processTemplate(String path, Map<String, String> placeholders) {
		// Load the template from the specified path
		EmailTemplate emailTemplate = loadTemplate(path);
		String processedTemplate = emailTemplate.getBody();
		for (Map.Entry<String, String> entry : placeholders.entrySet()) {
			String placeholder = entry.getKey();
			String value = entry.getValue();
			processedTemplate = processedTemplate.replace("{{" + placeholder + "}}", value);
		}
		return processedTemplate;
	}

	private EmailTemplate loadTemplate(String path) {
		ClassPathResource resource = new ClassPathResource(path);
		String content;
		try {
			content = new String(resource.getInputStream().readAllBytes());
		} catch (IOException e) {
			throw new IllegalArgumentException("Failed to load email template: " + path, e);
		}
		String[] parts = content.split("---BODY---");
		String subject = parts[0].replace("---SUBJECT---", "").trim();
		String body = parts.length > 1 ? parts[1].trim() : "";
		return new EmailTemplate(subject, body);
	}
}
