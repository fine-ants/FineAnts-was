package co.fineants.api.domain.member.properties;

import java.util.regex.Pattern;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;

import lombok.Getter;

@ConfigurationProperties(prefix = "member.email")
@Getter
public class EmailProperties {
	private final Pattern emailPattern;

	@ConstructorBinding
	public EmailProperties(String regex) {
		this.emailPattern = Pattern.compile(regex);
	}
}
