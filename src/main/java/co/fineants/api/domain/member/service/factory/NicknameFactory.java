package co.fineants.api.domain.member.service.factory;

import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

import co.fineants.api.domain.member.domain.entity.Nickname;
import co.fineants.api.domain.member.properties.NicknameProperties;
import co.fineants.api.global.errors.exception.business.NicknameInvalidInputException;

@Component
public class NicknameFactory {

	private final NicknameProperties nicknameProperties;

	public NicknameFactory(NicknameProperties nicknameProperties) {
		this.nicknameProperties = nicknameProperties;
	}

	public Nickname create(String value) {
		Pattern pattern = nicknameProperties.getNicknamePattern();
		if (!pattern.matcher(value).matches()) {
			throw new NicknameInvalidInputException(value);
		}
		return new Nickname(value);
	}
}
