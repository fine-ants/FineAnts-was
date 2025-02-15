package co.fineants.api.global.security.common.config;

import java.util.Collections;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.cors.CorsConfiguration;

@Configuration
@Profile(value = {"test", "local", "release"})
public class CommonSecurityDefaultConfig {

	@Bean
	public CorsConfiguration corsConfiguration() {
		CorsConfiguration config = new CorsConfiguration();
		config.setAllowedOrigins(
			List.of("https://fineants.co",
				"https://local.fineants.co:3000",
				"https://www.fineants.co",
				"https://release.fineants.co",
				"http://localhost:5173",
				"https://localhost:5173",
				"http://localhost:3000",
				"https://localhost:3000",
				"https://accounts.kakao.com/login",
				"https://nid.naver.com/oauth2.0/authorize",
				"https://accounts.google.com/o/oauth2/v2/auth"));
		config.setAllowedMethods(List.of("GET", "POST", "PUT", "UPDATE", "DELETE", "PATCH", "OPTIONS", "HEAD"));
		config.setAllowedHeaders(Collections.singletonList("*"));
		config.addExposedHeader("Set-Cookie");
		config.setAllowCredentials(true);
		config.setMaxAge(3600L);
		return config;
	}
}
