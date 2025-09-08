package co.fineants;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import org.springframework.security.test.context.support.WithSecurityContext;

@Retention(RetentionPolicy.RUNTIME)
@WithSecurityContext(factory = WithMockMemberAuthenticationSecurityContextFactory.class)
public @interface WithMockMemberAuthentication {
	String username() default "dragonbead95@naver.com";

	long memberId() default 1L;

	String[] roles() default {"ROLE_USER"};

	String provider() default "local";

	String profileUrl() default "profileUrl";
}
