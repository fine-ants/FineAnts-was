package co.fineants.api.domain.member.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;

import lombok.Getter;

@ConfigurationProperties(prefix = "member.nickname")
@Getter
public class NicknameProperties {
	private final String prefix;
	private final int len;

	@ConstructorBinding
	public NicknameProperties(String prefix, int len) {
		this.prefix = prefix;
		this.len = len;
	}

	@Override
	public String toString() {
		return String.format("NicknameProperties{prefix='%s', len=%d}", prefix, len);
	}
}
