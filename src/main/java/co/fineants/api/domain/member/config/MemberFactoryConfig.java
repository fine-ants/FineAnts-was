package co.fineants.api.domain.member.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;

import co.fineants.api.domain.member.domain.factory.MemberFactory;
import co.fineants.api.domain.member.domain.factory.MemberProfileFactory;
import co.fineants.api.domain.member.domain.factory.MemberRoleFactory;
import co.fineants.api.domain.member.domain.factory.VerifyCodeMimeMessageFactory;
import co.fineants.api.domain.member.service.MailHtmlRender;
import co.fineants.api.domain.member.service.RoleService;

@Configuration
public class MemberFactoryConfig {
	@Bean
	public MemberRoleFactory memberRoleFactory(RoleService roleService) {
		return new MemberRoleFactory(roleService);
	}

	@Bean
	public MemberFactory memberFactory() {
		return new MemberFactory();
	}

	@Bean
	public MemberProfileFactory memberProfileFactory() {
		return new MemberProfileFactory();
	}

	@Bean
	public VerifyCodeMimeMessageFactory verifyCodeMimeMessageFactory(MailHtmlRender render, JavaMailSender sender) {
		String subject = "Finants 회원가입 인증 코드";
		return new VerifyCodeMimeMessageFactory(render, sender, subject);
	}
}
