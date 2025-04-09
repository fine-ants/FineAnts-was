package co.fineants.api.infra.mail;

import static org.assertj.core.api.Assertions.*;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class EmailTemplateProcessorTest {

	private EmailTemplateProcessor emailTemplateProcessor;

	@BeforeEach
	void setUp() {
		emailTemplateProcessor = new EmailTemplateProcessor();
	}

	@DisplayName("환율 오류 API 메일 템플릿을 처리한다")
	@Test
	void processTemplate() {
		// given
		String path = "email/exchange-rate-fail-notification_template.txt";
		String failedAt = "2025-04-09T16:09:25.023737";
		String apiUrl = "https://exchange-rate-api1.p.rapidapi.com";
		String errormessage = "API 서버 오류";
		Map<String, String> placeholders = Map.of(
			"failedAt", failedAt,
			"apiUrl", apiUrl,
			"errorMessage", errormessage
		);
		// when
		String actual = emailTemplateProcessor.processTemplate(path, placeholders);
		// then
		assertThat(actual).contains(failedAt, apiUrl, errormessage);
	}
}
