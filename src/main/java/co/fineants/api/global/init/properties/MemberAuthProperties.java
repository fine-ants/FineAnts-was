package co.fineants.api.global.init.properties;

import org.springframework.boot.context.properties.bind.ConstructorBinding;

import lombok.Getter;

@Getter
public class MemberAuthProperties {
	private final String email;
	private final String nickname;
	private final String password;
	private final String provider;
	private final String roleName;

	@ConstructorBinding
	public MemberAuthProperties(String email, String nickname, String password, String provider, String roleName) {
		this.email = email;
		this.nickname = nickname;
		this.password = password;
		this.provider = provider;
		this.roleName = roleName;
	}
}
