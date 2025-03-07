package co.fineants.api.global.security.ajax.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.SecurityFilterChain;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class ActuatorSecurityConfig {
	private final UserDetailsService actuatorUserDetailService;
	private final ActuatorProperties actuatorProperties;

	// actuator 엔드포인트의 대한 기본 인증 Security Filter Chain 설정
	@Bean
	@Order(1)
	protected SecurityFilterChain actuatorSecurityFilterChain(HttpSecurity http) throws Exception {
		http
			.httpBasic(configurer -> {
			})
			.securityMatcher("/actuator", "/actuator/**", "/login")
			.authorizeHttpRequests(configurer ->
				configurer
					.requestMatchers("/actuator").hasRole(actuatorProperties.getRoleName())
					.requestMatchers("/actuator/**").hasRole(actuatorProperties.getRoleName())
					.requestMatchers("/login").permitAll()
					.anyRequest().authenticated()
			)
			.formLogin(configurer -> {
			})
			.csrf(AbstractHttpConfigurer::disable);
		http.logout(configurer -> {
		});
		http.userDetailsService(actuatorUserDetailService);
		return http.build();
	}
}
