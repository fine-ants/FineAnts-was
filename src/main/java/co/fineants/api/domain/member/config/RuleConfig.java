package co.fineants.api.domain.member.config;

import java.util.regex.Pattern;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import co.fineants.api.domain.member.domain.rule.EmailDuplicationRule;
import co.fineants.api.domain.member.domain.rule.EmailFormatRule;
import co.fineants.api.domain.member.domain.rule.NicknameDuplicationRule;
import co.fineants.api.domain.member.domain.rule.NicknameFormatRule;
import co.fineants.api.domain.member.domain.rule.NicknameValidator;
import co.fineants.api.domain.member.domain.rule.SignUpValidator;
import co.fineants.api.domain.member.domain.rule.ValidationRule;
import co.fineants.api.domain.member.repository.MemberRepository;

@Configuration
public class RuleConfig {

	public static final Pattern NICKNAME_PATTERN = Pattern.compile("^[가-힣a-zA-Z0-9]{2,10}$");
	public static final Pattern EMAIL_PATTERN = Pattern.compile("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$");

	@Bean
	public EmailFormatRule emailFormatRule() {
		return new EmailFormatRule(EMAIL_PATTERN);
	}

	@Bean
	public EmailDuplicationRule emailDuplicationRule(MemberRepository memberRepository) {
		return new EmailDuplicationRule(memberRepository);
	}

	@Bean
	public NicknameFormatRule nicknameFormatRule() {
		return new NicknameFormatRule(NICKNAME_PATTERN);
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
}
