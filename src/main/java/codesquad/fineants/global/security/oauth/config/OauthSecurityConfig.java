package codesquad.fineants.global.security.oauth.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestRedirectFilter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.AuthorizationFilter;
import org.springframework.web.cors.CorsConfiguration;

import codesquad.fineants.domain.member.repository.MemberRepository;
import codesquad.fineants.domain.member.repository.RoleRepository;
import codesquad.fineants.domain.member.service.NicknameGenerator;
import codesquad.fineants.domain.member.service.OauthMemberRedisService;
import codesquad.fineants.domain.notificationpreference.repository.NotificationPreferenceRepository;
import codesquad.fineants.global.security.ajax.entrypoint.CommonLoginAuthenticationEntryPoint;
import codesquad.fineants.global.security.factory.TokenFactory;
import codesquad.fineants.global.security.handler.CustomAccessDeniedHandler;
import codesquad.fineants.global.security.oauth.filter.JwtAuthenticationFilter;
import codesquad.fineants.global.security.oauth.filter.OAuth2AuthorizationRequestRedirectWithRedirectUrlParamFilter;
import codesquad.fineants.global.security.oauth.handler.OAuth2SuccessHandler;
import codesquad.fineants.global.security.oauth.handler.OAuth2UserMapper;
import codesquad.fineants.global.security.oauth.service.CustomOAuth2UserService;
import codesquad.fineants.global.security.oauth.service.CustomOidcUserService;
import codesquad.fineants.global.security.oauth.service.TokenService;

@Configuration
@EnableMethodSecurity(securedEnabled = true)
public class OauthSecurityConfig {

	private final MemberRepository memberRepository;
	private final NotificationPreferenceRepository notificationPreferenceRepository;
	private final TokenService tokenService;
	private final NicknameGenerator nicknameGenerator;
	private final RoleRepository roleRepository;
	private final OAuth2UserMapper oAuth2UserMapper;
	private final CommonLoginAuthenticationEntryPoint commonLoginAuthenticationEntryPoint;
	private final OauthMemberRedisService oauthMemberRedisService;
	private final String loginSuccessUri;
	private final TokenFactory tokenFactory;
	private final CorsConfiguration corsConfiguration;

	public OauthSecurityConfig(MemberRepository memberRepository,
		NotificationPreferenceRepository notificationPreferenceRepository, TokenService tokenService,
		NicknameGenerator nicknameGenerator, RoleRepository roleRepository, OAuth2UserMapper oAuth2UserMapper,
		CommonLoginAuthenticationEntryPoint commonLoginAuthenticationEntryPoint,
		OauthMemberRedisService oauthMemberRedisService, @Value("${oauth2.login-success-uri}") String loginSuccessUri,
		TokenFactory tokenFactory, CorsConfiguration corsConfiguration) {
		this.memberRepository = memberRepository;
		this.notificationPreferenceRepository = notificationPreferenceRepository;
		this.tokenService = tokenService;
		this.nicknameGenerator = nicknameGenerator;
		this.roleRepository = roleRepository;
		this.oAuth2UserMapper = oAuth2UserMapper;
		this.commonLoginAuthenticationEntryPoint = commonLoginAuthenticationEntryPoint;
		this.oauthMemberRedisService = oauthMemberRedisService;
		this.loginSuccessUri = loginSuccessUri;
		this.tokenFactory = tokenFactory;
		this.corsConfiguration = corsConfiguration;
	}

	@Bean
	@Order(1)
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http
			.authorizeHttpRequests(authorize ->
				authorize
					.requestMatchers(
						"/oauth2/authorization/**",
						"/login/oauth2/code/**",
						"/api/oauth/redirect",
						"/api/auth/signup",
						"/api/auth/signup/duplicationcheck/nickname/**",
						"/api/auth/signup/duplicationcheck/email/**",
						"/api/auth/signup/verifyEmail",
						"/api/auth/signup/verifyCode",
						"/api/auth/refresh/token",
						"/api/stocks/search",
						"/api/stocks/**",
						"/error"
					).permitAll()
					.anyRequest().authenticated());
		http
			.sessionManagement(configurer -> configurer
				.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
		http.addFilterBefore(new OAuth2AuthorizationRequestRedirectWithRedirectUrlParamFilter(),
			OAuth2AuthorizationRequestRedirectFilter.class);
		http.addFilterBefore(new JwtAuthenticationFilter(tokenService, oauthMemberRedisService, tokenFactory),
			AuthorizationFilter.class);

		http
			.oauth2Login(configurer -> configurer
				.userInfoEndpoint(config -> config
					.userService(customOAuth2UserService())
					.oidcUserService(customOidcUserService())
				)
				.successHandler(oauth2SuccessHandler()));
		http.exceptionHandling(configurer -> configurer
			.authenticationEntryPoint(commonLoginAuthenticationEntryPoint)
			.accessDeniedHandler(customAccessDeniedHandler()));
		http.cors(configurer -> configurer.configurationSource(request -> corsConfiguration));
		http.csrf(AbstractHttpConfigurer::disable);

		return http.build();
	}

	@Bean
	public CustomOAuth2UserService customOAuth2UserService() {
		return new CustomOAuth2UserService(memberRepository, notificationPreferenceRepository, nicknameGenerator,
			roleRepository);
	}

	@Bean
	public CustomOidcUserService customOidcUserService() {
		return new CustomOidcUserService(memberRepository, notificationPreferenceRepository, nicknameGenerator,
			roleRepository);
	}

	@Bean
	public OAuth2SuccessHandler oauth2SuccessHandler() {
		return new OAuth2SuccessHandler(tokenService, oAuth2UserMapper, loginSuccessUri, tokenFactory);
	}

	@Bean
	public WebSecurityCustomizer webSecurity() {
		return (web) -> web.ignoring()
			.requestMatchers("/docs/**")
			.requestMatchers("/static/docs/**")
			.requestMatchers(PathRequest.toStaticResources().atCommonLocations());
	}

	@Bean
	public CustomAccessDeniedHandler customAccessDeniedHandler() {
		return new CustomAccessDeniedHandler();
	}
}
