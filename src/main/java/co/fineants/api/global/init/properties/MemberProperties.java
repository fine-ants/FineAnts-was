package co.fineants.api.global.init.properties;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;

import lombok.Getter;

@Getter
@ConfigurationProperties(value = "member")
public class MemberProperties {
	private final List<MemberAuthProperty> properties;

	@ConstructorBinding
	public MemberProperties(List<MemberAuthProperty> properties) {
		this.properties = properties;
	}

	@Getter
	public static class MemberAuthProperty {
		private final String email;
		private final String nickname;
		private final String password;
		private final String provider;
		private final String roleName;

		@ConstructorBinding
		public MemberAuthProperty(String email, String nickname, String password, String provider, String roleName) {
			this.email = email;
			this.nickname = nickname;
			this.password = password;
			this.provider = provider;
			this.roleName = roleName;
		}
	}
}
