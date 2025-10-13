package co.fineants.api.domain.member.service;

import static org.apache.logging.log4j.util.Strings.*;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import co.fineants.api.domain.member.domain.entity.Nickname;
import co.fineants.api.domain.member.service.factory.NicknameFactory;

@Component
public class NicknameGenerator {

	private static final String HYPHEN = "-";

	private final String prefix;
	private final int len;
	private final NicknameFactory factory;

	public NicknameGenerator(
		@Value("${member.nickname.prefix}") String prefix,
		@Value("${member.nickname.len}") int len,
		NicknameFactory factory) {
		this.prefix = prefix;
		this.len = len;
		this.factory = factory;
	}

	public Nickname generate() {
		String value = String.join(EMPTY, prefix,
			UUID.randomUUID().toString().replace(HYPHEN, EMPTY).substring(0, len));
		return factory.create(value);
	}
}
