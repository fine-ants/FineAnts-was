package co.fineants.api.domain.validator.domain.member;

import co.fineants.api.domain.member.domain.entity.Member;
import co.fineants.api.domain.member.service.NicknameDuplicateValidator;
import co.fineants.api.domain.validator.domain.MemberValidationRule;
import co.fineants.api.global.errors.exception.business.NicknameDuplicateException;

public class NicknameDuplicationRule implements MemberValidationRule {

	private final NicknameDuplicateValidator validator;

	public NicknameDuplicationRule(NicknameDuplicateValidator validator) {
		this.validator = validator;
	}

	@Override
	public void validate(String nickname) {
		if (validator.isDuplicate(nickname)) {
			throw new NicknameDuplicateException(nickname);
		}
	}

	@Override
	public void validate(Member member) {
		validate(member.getNickname());
	}
}
