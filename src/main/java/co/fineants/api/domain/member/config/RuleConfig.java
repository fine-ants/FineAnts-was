package co.fineants.api.domain.member.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import co.fineants.api.domain.validator.member.EmailDuplicationRule;
import co.fineants.api.domain.validator.member.EmailFormatRule;
import co.fineants.api.domain.validator.member.EmailValidator;
import co.fineants.api.domain.validator.member.NicknameDuplicationRule;
import co.fineants.api.domain.validator.member.NicknameFormatRule;
import co.fineants.api.domain.validator.member.NicknameValidator;
import co.fineants.api.domain.validator.member.PasswordValidator;
import co.fineants.api.domain.validator.member.SignUpValidator;
import co.fineants.api.domain.validator.MemberValidationRule;
import co.fineants.api.domain.member.properties.EmailProperties;
import co.fineants.api.domain.member.properties.NicknameProperties;
import co.fineants.api.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class RuleConfig {

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
	public NicknameDuplicationRule nicknameDuplicationRule(MemberRepository memberRepository) {
		return new NicknameDuplicationRule(memberRepository);
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
