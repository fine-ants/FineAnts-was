package co.fineants.api.domain.member.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import co.fineants.api.domain.member.domain.rule.EmailDuplicationRule;
import co.fineants.api.domain.member.domain.rule.EmailFormatRule;
import co.fineants.api.domain.member.domain.rule.NicknameDuplicationRule;
import co.fineants.api.domain.member.domain.rule.NicknameFormatRule;
import co.fineants.api.domain.member.domain.rule.SignUpValidator;
import co.fineants.api.domain.member.domain.rule.ValidationRule;
import co.fineants.api.domain.member.repository.MemberRepository;
import co.fineants.api.domain.member.service.MemberService;

@Configuration
public class RuleConfig {

	@Bean
	public EmailFormatRule emailFormatRule() {
		return new EmailFormatRule(MemberService.EMAIL_PATTERN);
	}

	@Bean
	public EmailDuplicationRule emailDuplicationRule(MemberRepository memberRepository) {
		return new EmailDuplicationRule(memberRepository);
	}

	@Bean
	public NicknameFormatRule nicknameFormatRule() {
		return new NicknameFormatRule(MemberService.NICKNAME_PATTERN);
	}

	@Bean
	public NicknameDuplicationRule nicknameDuplicationRule(MemberRepository memberRepository) {
		return new NicknameDuplicationRule(memberRepository);
	}

	@Bean
	public SignUpValidator signUpValidator(
		EmailFormatRule emailFormatRule,
		EmailDuplicationRule emailDuplicationRule,
		NicknameFormatRule nicknameFormatRule,
		NicknameDuplicationRule nicknameDuplicationRule) {
		ValidationRule[] rules = {
			emailFormatRule,
			emailDuplicationRule,
			nicknameFormatRule,
			nicknameDuplicationRule
		};
		return new SignUpValidator(rules);
	}
}
