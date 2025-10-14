package co.fineants.api.domain.validator.domain.member;

import co.fineants.api.domain.member.domain.entity.Member;
import co.fineants.api.domain.member.domain.entity.Nickname;
import co.fineants.api.domain.validator.domain.MemberValidationRule;
import co.fineants.api.global.errors.exception.business.NicknameInvalidInputException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NicknameFormatRule implements MemberValidationRule {

	@Override
	public void validate(String nickname) {
		try {
			Nickname memberNickname = new Nickname(nickname);
			log.info("Validated nickname: {}", memberNickname);
		} catch (IllegalArgumentException e) {
			throw new NicknameInvalidInputException(nickname);
		}
	}

	@Override
	public void validate(Member member) {
		validate(member.getNickname());
	}
}
