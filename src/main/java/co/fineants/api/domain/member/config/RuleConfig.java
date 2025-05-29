package co.fineants.api.domain.member.config;

import java.util.regex.Pattern;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import co.fineants.api.domain.member.domain.rule.EmailDuplicationRule;
import co.fineants.api.domain.member.domain.rule.EmailFormatRule;
import co.fineants.api.domain.member.domain.rule.EmailValidator;
import co.fineants.api.domain.member.domain.rule.NicknameDuplicationRule;
import co.fineants.api.domain.member.domain.rule.NicknameFormatRule;
import co.fineants.api.domain.member.domain.rule.NicknameValidator;
import co.fineants.api.domain.member.domain.rule.PasswordValidator;
import co.fineants.api.domain.member.domain.rule.SignUpValidator;
import co.fineants.api.domain.member.domain.rule.ValidationRule;
import co.fineants.api.domain.member.properties.EmailProperties;
import co.fineants.api.domain.member.properties.NicknameProperties;
import co.fineants.api.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class RuleConfig {

	public static final Pattern NICKNAME_PATTERN = Pattern.compile("^[가-힣a-zA-Z0-9]{2,10}$");

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
		ValidationRule[] rules = {
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
