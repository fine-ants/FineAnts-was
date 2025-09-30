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
}
