package co.fineants.api.domain.member.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;

import co.fineants.api.domain.member.domain.factory.MemberFactory;
import co.fineants.api.domain.member.domain.factory.MemberProfileFactory;
import co.fineants.api.domain.member.domain.factory.MemberRoleFactory;
import co.fineants.api.domain.member.domain.factory.MimeMessageFactory;
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
	public MimeMessageFactory mimeMessageFactory(JavaMailSender mailSender) {
		return new MimeMessageFactory(mailSender);
	}
}
