package co.fineants.api.domain.validator.domain.member;

import co.fineants.member.domain.Member;
import co.fineants.member.domain.Nickname;
import co.fineants.api.domain.member.service.NicknameDuplicateValidator;
import co.fineants.api.domain.validator.domain.MemberValidationRule;
import co.fineants.api.global.errors.exception.business.NicknameDuplicateException;

public class NicknameDuplicationRule implements MemberValidationRule {

	private final NicknameDuplicateValidator validator;

	public NicknameDuplicationRule(NicknameDuplicateValidator validator) {
		this.validator = validator;
	}

	@Override
	public void validate(String value) {
		Nickname nickname = new Nickname(value);
		if (validator.isDuplicate(nickname)) {
			throw new NicknameDuplicateException(nickname.getValue());
		}
	}

	@Override
	public void validate(Member member) {
		validate(member.getNickname());
	}
}
