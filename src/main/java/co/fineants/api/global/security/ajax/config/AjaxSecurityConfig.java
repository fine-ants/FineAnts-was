package co.fineants.api.global.security.ajax.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;

import com.fasterxml.jackson.databind.ObjectMapper;

import co.fineants.api.domain.member.service.MemberService;
import co.fineants.api.global.security.ajax.entrypoint.CommonLoginAuthenticationEntryPoint;
import co.fineants.api.global.security.ajax.filter.AjaxLoginProcessingFilter;
import co.fineants.api.global.security.ajax.handler.AjaxAuthenticationFailHandler;
import co.fineants.api.global.security.ajax.handler.AjaxAuthenticationSuccessHandler;
import co.fineants.api.global.security.ajax.handler.AjaxLogoutHandler;
import co.fineants.api.global.security.ajax.provider.AjaxAuthenticationProvider;
import co.fineants.api.global.security.factory.TokenFactory;
import co.fineants.api.global.security.handler.JwtLogoutSuccessHandler;
import co.fineants.api.global.security.oauth.service.TokenService;
import jakarta.servlet.DispatcherType;
import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class AjaxSecurityConfig {
	private static final String LOGIN_ENDPOINT = "/api/auth/login";
	private static final String LOGOUT_ENDPOINT = "/api/auth/logout";
	private static final String ERROR_ENDPOINT = "/error";
	private static final String ACTUATOR_ENDPOINT = "/actuator/**";

	private final UserDetailsService userDetailsService;
	private final PasswordEncoder passwordEncoder;
	private final ObjectMapper objectMapper;
	private final TokenService tokenService;
	private final MemberService memberService;
	private final TokenFactory tokenFactory;
	private final CorsConfiguration corsConfiguration;
	private final ActuatorProperties actuatorProperties;

	@Bean
	@Order(0)
	protected SecurityFilterChain ajaxSecurityFilterChain(HttpSecurity http) throws Exception {
		http.sessionManagement(configurer -> configurer.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
		http
			.securityMatcher(
				LOGIN_ENDPOINT,
				LOGOUT_ENDPOINT,
				ERROR_ENDPOINT
			)
			.authorizeHttpRequests(authorize -> authorize
				.requestMatchers(LOGIN_ENDPOINT).permitAll()
				.requestMatchers(LOGOUT_ENDPOINT).permitAll()
				.requestMatchers(ERROR_ENDPOINT).permitAll()
				.dispatcherTypeMatchers(DispatcherType.ASYNC, DispatcherType.FORWARD, DispatcherType.ERROR).permitAll()
				.anyRequest().authenticated()
			);
		http.authenticationProvider(authenticationProvider());
		http.authenticationManager(authenticationManager());

		AjaxLoginProcessingFilter ajaxLoginProcessingFilter = new AjaxLoginProcessingFilter(
			new AntPathRequestMatcher(LOGIN_ENDPOINT),
			authenticationManager(),
			objectMapper);
		ajaxLoginProcessingFilter.setAuthenticationSuccessHandler(ajaxAuthenticationSuccessHandler());
		ajaxLoginProcessingFilter.setAuthenticationFailureHandler(ajaxAuthenticationFailHandler());
		http.addFilterBefore(ajaxLoginProcessingFilter, UsernamePasswordAuthenticationFilter.class);

		http.logout(configurer -> configurer
			.logoutUrl(LOGOUT_ENDPOINT)
			.addLogoutHandler(logoutHandler())
			.logoutSuccessHandler(jwtLogoutSuccessHandler())
			.permitAll()
		);

		http.exceptionHandling(configurer ->
			configurer.authenticationEntryPoint(commonLoginAuthenticationEntryPoint()));
		http.cors(configurer -> configurer.configurationSource(request -> corsConfiguration));
		http.csrf(AbstractHttpConfigurer::disable);
		return http.build();
	}

	// basic auth를 위한 Security Filter Chain 설정
	@Bean
	@Order(1)
	protected SecurityFilterChain actuatorSecurityFilterChain(HttpSecurity http) throws Exception {
		http
			.httpBasic(configurer -> {
			})
			.authorizeHttpRequests(configurer ->
				configurer.requestMatchers(ACTUATOR_ENDPOINT).hasRole("ACTUATOR")
					.anyRequest().authenticated()
			)
			.formLogin(configurer -> {
			})
			.csrf(AbstractHttpConfigurer::disable);
		http.userDetailsService(inMemoryUserDetailsManager());
		return http.build();
	}

	@Bean
	protected UserDetailsService inMemoryUserDetailsManager() {
		InMemoryUserDetailsManager manager = new InMemoryUserDetailsManager();
		String encodedPassword = passwordEncoder.encode(actuatorProperties.getPassword());
		UserDetails userDetails = User.withUsername(actuatorProperties.getUser())
			.password(encodedPassword)
			.roles(actuatorProperties.getRoleName())
			.build();
		manager.createUser(userDetails);
		return manager;
	}

	@Bean
	protected AuthenticationManager authenticationManager() {
		return new ProviderManager(authenticationProvider());
	}

	@Bean
	protected AuthenticationProvider authenticationProvider() {
		return new AjaxAuthenticationProvider(userDetailsService, passwordEncoder);
	}

	@Bean
	protected LogoutHandler logoutHandler() {
		return new AjaxLogoutHandler(memberService);
	}

	@Bean
	protected JwtLogoutSuccessHandler jwtLogoutSuccessHandler() {
		return new JwtLogoutSuccessHandler(objectMapper);
	}

	@Bean
	protected CommonLoginAuthenticationEntryPoint commonLoginAuthenticationEntryPoint() {
		return new CommonLoginAuthenticationEntryPoint(objectMapper);
	}

	@Bean
	protected AjaxAuthenticationSuccessHandler ajaxAuthenticationSuccessHandler() {
		return new AjaxAuthenticationSuccessHandler(objectMapper, tokenService, tokenFactory);
	}

	@Bean
	protected AjaxAuthenticationFailHandler ajaxAuthenticationFailHandler() {
		return new AjaxAuthenticationFailHandler(objectMapper);
	}
}
