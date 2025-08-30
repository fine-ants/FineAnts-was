package co.fineants.api.domain.member.service;

import static org.assertj.core.api.Assertions.*;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import co.fineants.AbstractContainerBaseTest;
import co.fineants.api.global.errors.exception.business.ExternalApiGetRequestException;

class ExchangeRateErrorMailHtmlRenderTest extends AbstractContainerBaseTest {

	@Autowired
	private ExchangeRateErrorMailHtmlRender render;

	@DisplayName("환율 업데이트 실패 알림 HTML 렌더링 테스트")
	@Test
	void givenExternalApiGetRequestException_whenRender_thenReturnHtml() {
		// given
		ExternalApiGetRequestException exception = new ExternalApiGetRequestException("Failed to fetch exchange rates",
			HttpStatus.BAD_REQUEST);
		String apiUrl = "https://exchange-rate-api1.p.rapidapi.com/latest";
		StringWriter sw = new StringWriter();
		exception.printStackTrace(new PrintWriter(sw));
		String stackTrace = sw.toString();
		LocalDateTime failedAt = LocalDateTime.parse("2025-05-28T15:06:49.189586");
		Map<String, Object> variables = Map.of(
			"failedAt", failedAt.toString(),
			"apiUrl", apiUrl,
			"errorMessage", exception.getErrorCodeMessage(),
			"stackTrace", stackTrace
		);
		// when
		String html = render.render(variables);
		// then
		assertThat(html)
			.contains(failedAt.toString())
			.contains(apiUrl)
			.contains(exception.getErrorCodeMessage());
	}
}
