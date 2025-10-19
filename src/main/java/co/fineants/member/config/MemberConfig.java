package co.fineants.member.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import co.fineants.member.infrastructure.SpringMemberPasswordEncoder;

@Configuration
public class MemberConfig {
	@Bean
	public SpringMemberPasswordEncoder springMemberPasswordEncoder(PasswordEncoder passwordEncoder) {
		return new SpringMemberPasswordEncoder(passwordEncoder);
	}
}
