package co.fineants.api.domain.member.service.factory;

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
		if (value == null || value.isBlank()) {
			throw new NicknameInvalidInputException(value);
		}
		if (!nicknameProperties.getNicknamePattern().matcher(value).matches()) {
			throw new NicknameInvalidInputException(value);
		}
		return new Nickname(value);
	}
}
