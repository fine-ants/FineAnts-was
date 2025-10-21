package co.fineants.api.global.security.ajax.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;

import co.fineants.member.domain.MemberPasswordEncoder;
import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class ActuatorConfig {

	private final MemberPasswordEncoder passwordEncoder;
	private final ActuatorProperties actuatorProperties;

	@Bean(name = "actuatorUserDetailService")
	protected UserDetailsService actuatorUserDetailService() {
		InMemoryUserDetailsManager manager = new InMemoryUserDetailsManager();
		String encodedPassword = passwordEncoder.encode(actuatorProperties.getPassword());
		UserDetails userDetails = User.withUsername(actuatorProperties.getUser())
			.password(encodedPassword)
			.roles(actuatorProperties.getRoleName())
			.build();
		manager.createUser(userDetails);
		return manager;
	}
}
