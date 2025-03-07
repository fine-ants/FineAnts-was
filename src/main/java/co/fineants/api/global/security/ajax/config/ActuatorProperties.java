package co.fineants.api.global.security.ajax.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;

import lombok.Getter;

@ConfigurationProperties(prefix = "actuator")
@Getter
public class ActuatorProperties {
	private final String user;
	private final String password;
	private final String roleName;

	@ConstructorBinding
	public ActuatorProperties(String user, String password, String roleName) {
		this.user = user;
		this.password = password;
		this.roleName = roleName;
	}
}
