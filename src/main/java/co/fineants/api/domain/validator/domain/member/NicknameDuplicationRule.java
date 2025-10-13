package co.fineants.api.domain.validator.domain.member;

import co.fineants.api.domain.member.domain.entity.Member;
import co.fineants.api.domain.member.domain.entity.Nickname;
import co.fineants.api.domain.member.service.NicknameDuplicateValidator;
import co.fineants.api.domain.member.service.factory.NicknameFactory;
import co.fineants.api.domain.validator.domain.MemberValidationRule;
import co.fineants.api.global.errors.exception.business.NicknameDuplicateException;

public class NicknameDuplicationRule implements MemberValidationRule {

	private final NicknameDuplicateValidator validator;
	private final NicknameFactory factory;

	public NicknameDuplicationRule(NicknameDuplicateValidator validator, NicknameFactory factory) {
		this.validator = validator;
		this.factory = factory;
	}

	@Override
	public void validate(String value) {
		Nickname nickname = factory.create(value);
		if (validator.isDuplicate(nickname)) {
			throw new NicknameDuplicateException(nickname.getValue());
		}
	}

	@Override
	public void validate(Member member) {
		validate(member.getNickname());
	}
}
