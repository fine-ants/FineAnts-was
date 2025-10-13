package co.fineants.api.domain.validator.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import co.fineants.api.domain.member.properties.EmailProperties;
import co.fineants.api.domain.member.properties.NicknameProperties;
import co.fineants.api.domain.member.repository.MemberRepository;
import co.fineants.api.domain.member.service.NicknameDuplicateValidator;
import co.fineants.api.domain.validator.domain.MemberValidationRule;
import co.fineants.api.domain.validator.domain.member.EmailDuplicationRule;
import co.fineants.api.domain.validator.domain.member.EmailFormatRule;
import co.fineants.api.domain.validator.domain.member.EmailValidator;
import co.fineants.api.domain.validator.domain.member.NicknameDuplicationRule;
import co.fineants.api.domain.validator.domain.member.NicknameFormatRule;
import co.fineants.api.domain.validator.domain.member.NicknameValidator;
import co.fineants.api.domain.validator.domain.member.PasswordValidator;
import co.fineants.api.domain.validator.domain.member.SignUpValidator;

@Configuration
public class MemberRuleConfig {

	@Bean
	public EmailFormatRule emailFormatRule(EmailProperties emailProperties) {
		return new EmailFormatRule(emailProperties.getEmailPattern());
	}

	@Bean
	public EmailDuplicationRule emailDuplicationRule(MemberRepository memberRepository) {
		return new EmailDuplicationRule(memberRepository, "local");
	}

	@Bean
	public NicknameFormatRule nicknameFormatRule(NicknameProperties nicknameProperties) {
		return new NicknameFormatRule(nicknameProperties.getNicknamePattern());
	}

	@Bean
	public NicknameDuplicationRule nicknameDuplicationRule(NicknameDuplicateValidator validator) {
		return new NicknameDuplicationRule(validator);
	}

	@Bean
	public SignUpValidator signUpValidator(
		EmailFormatRule emailFormatRule,
		EmailDuplicationRule emailDuplicationRule,
		NicknameFormatRule nicknameFormatRule,
		NicknameDuplicationRule nicknameDuplicationRule) {
		MemberValidationRule[] rules = {
			emailFormatRule,
			emailDuplicationRule,
			nicknameFormatRule,
			nicknameDuplicationRule
		};
		return new SignUpValidator(rules);
	}

	@Bean
	public NicknameValidator nicknameValidator(NicknameFormatRule nicknameFormatRule,
		NicknameDuplicationRule nicknameDuplicationRule) {
		return new NicknameValidator(nicknameFormatRule, nicknameDuplicationRule);
	}

	@Bean
	public EmailValidator emailValidator(EmailFormatRule emailFormatRule,
		EmailDuplicationRule emailDuplicationRule) {
		return new EmailValidator(emailFormatRule, emailDuplicationRule);
	}

	@Bean
	public PasswordValidator passwordValidator() {
		return new PasswordValidator();
	}
}
