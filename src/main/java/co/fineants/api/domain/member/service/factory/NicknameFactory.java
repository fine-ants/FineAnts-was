package co.fineants.api.domain.member.service.factory;

import org.springframework.stereotype.Component;

import co.fineants.api.domain.member.domain.entity.Nickname;

@Component
public class NicknameFactory {
	public Nickname create(String value) {
		return new Nickname(value);
	}
}
