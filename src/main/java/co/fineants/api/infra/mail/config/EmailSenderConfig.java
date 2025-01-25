package co.fineants.api.infra.mail.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

@Configuration
public class EmailSenderConfig {
	@Bean
	public JavaMailSender javaMailSender() {
		return new JavaMailSenderImpl();
	}
}
