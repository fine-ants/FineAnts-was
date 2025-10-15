package co.fineants.api.infra.mail;

import java.util.Map;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.thymeleaf.spring6.SpringTemplateEngine;

import co.fineants.AbstractContainerBaseTest;

class VerifyCodeMailHtmlRenderTest extends AbstractContainerBaseTest {

	private MailHtmlRender mailHtmlRender;

	@Autowired
	private SpringTemplateEngine engine;

	@BeforeEach
	void setUp() {
		String templateName = "mail-templates/verify-email_template";
		mailHtmlRender = new VerifyCodeMailHtmlRender(templateName, engine);
	}

	@DisplayName("인증 코드가 주어지고 메일 내용에 해당하는 HTML을 생성한다.")
	@Test
	void givenVerifyCode_whenRender_thenReturnHtml() {
		// given
		Map<String, Object> variables = Map.of("verifyCode", "123456");
		// when
		String html = mailHtmlRender.render(variables);
		// then
		String expected = """
			<!doctype html>
			<html lang="ko">
			<head>
			    <meta charset="UTF-8">
			    <meta name="viewport"
			          content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
			    <meta http-equiv="X-UA-Compatible" content="IE=edge">
			    <title>회원가입 인증 코드</title>
			</head>
			<body style="font-family: Arial, sans-serif; background-color: #f9f9f9; margin: 0; padding: 0;">
			<div style="max-width: 600px; margin: 40px auto; background-color: #ffffff; padding: 30px; text-align: center; border-radius: 8px; box-shadow: 0 0 10px rgba(0,0,0,0.1);">
			    <h2 style="color: #333;">회원가입 인증 코드</h2>
			    <p style="font-size: 16px; color: #555;">아래 인증 코드를 회원가입 페이지에 입력해주세요.</p>
			    <div style="margin-top: 20px; font-size: 24px; font-weight: bold; color: #2c3e50;">
			        <span>123456</span>
			    </div>
			</div>
			</body>
			</html>
			""";
		Assertions.assertThat(html).isEqualTo(expected);
	}
}
