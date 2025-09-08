package co.fineants;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

import co.fineants.api.global.security.oauth.dto.MemberAuthentication;

public class WithMockMemberAuthenticationSecurityContextFactory
	implements WithSecurityContextFactory<WithMockMemberAuthentication> {

	@Override
	public SecurityContext createSecurityContext(WithMockMemberAuthentication annotation) {
		SecurityContext context = SecurityContextHolder.createEmptyContext();

		List<SimpleGrantedAuthority> authorities = Arrays.stream(annotation.roles())
			.map(SimpleGrantedAuthority::new)
			.toList();

		// 여기서 직접 MemberAuthentication 객체 생성
		MemberAuthentication principal = MemberAuthentication.create(
			annotation.memberId(),
			annotation.username(),
			"ants1234",
			annotation.provider(),
			annotation.profileUrl(),
			Set.of(annotation.roles())
		);

		Authentication auth =
			new UsernamePasswordAuthenticationToken(principal, "password", authorities);
		context.setAuthentication(auth);
		return context;
	}
}
