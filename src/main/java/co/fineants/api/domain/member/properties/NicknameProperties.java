package co.fineants.api.domain.member.properties;

import java.util.regex.Pattern;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;

import lombok.Getter;

@ConfigurationProperties(prefix = "member.nickname")
@Getter
public class NicknameProperties {
	private final String prefix;
	private final int len;
	private final Pattern nicknamePattern;

	@ConstructorBinding
	public NicknameProperties(String prefix, int len, String regex) {
		this.prefix = prefix;
		this.len = len;
		this.nicknamePattern = Pattern.compile(regex);
	}

	@Override
	public String toString() {
		return String.format("NicknameProperties{prefix='%s', len=%d, regex='%s'}", prefix, len,
			nicknamePattern.pattern());
	}
}
