package co.fineants.api.global.common.authorized;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.apache.logging.log4j.util.Strings;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.context.ApplicationContext;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import co.fineants.api.global.common.authorized.service.AuthorizedService;
import co.fineants.api.global.common.resource.ResourceIdParser;
import co.fineants.api.global.errors.errorcode.ErrorCode;
import co.fineants.api.global.errors.exception.temp.AuthorizationException;
import co.fineants.api.global.errors.exception.temp.MemberAuthenticationException;
import co.fineants.api.global.security.oauth.dto.MemberAuthentication;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class AuthorizationAspect {

	private final ApplicationContext applicationContext;

	private final ResourceIdParser resourceIdParser;

	@Before(value = "@annotation(authorized) && args(..)")
	public void validatePortfolioAuthorization(JoinPoint joinPoint, Authorized authorized) {
		AuthorizedService<?> service = (AuthorizedService<?>)applicationContext.getBean(authorized.serviceClass());

		List<Long> resourceIds = getResourceId((ProceedingJoinPoint)joinPoint);
		List<?> resources = service.findResourceAllBy(resourceIds);
		Long memberId = getLoggedInMemberId();

		resources.stream()
			.filter(resource -> !service.isAuthorized(resource, memberId))
			.forEach(resource -> {
				log.error("User with memberId {} have invalid authorization for resourceIds {}", memberId, resourceIds);
				throw new AuthorizationException(resource.toString(), ErrorCode.AUTHORIZATION);
			});
		log.info("User with memberId {} has valid authorization for resourceIds {}.", memberId, resourceIds);
	}

	private List<Long> getResourceId(ProceedingJoinPoint joinPoint) {
		try {
			return resourceIdParser.getResourceList(joinPoint);
		} catch (Exception e) {
			log.error(e.getMessage());
			return Collections.emptyList();
		}
	}

	private Long getLoggedInMemberId() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		Object principal = authentication.getPrincipal();
		MemberAuthentication memberAuthentication = (MemberAuthentication)principal;
		return Optional.ofNullable(memberAuthentication).map(MemberAuthentication::getId)
			.orElseThrow(() -> new MemberAuthenticationException(Strings.EMPTY));
	}
}
