package co.fineants.api.global.security.oauth.service;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.transaction.annotation.Transactional;

import co.fineants.api.global.security.oauth.dto.OAuthAttribute;
import co.fineants.member.application.NicknameGenerator;
import co.fineants.member.domain.Member;
import co.fineants.member.domain.MemberRepository;
import co.fineants.role.application.FindRole;
import co.fineants.role.domain.Role;
import co.fineants.role.domain.RoleRepository;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CustomOidcUserService extends AbstractUserService implements OAuth2UserService<OidcUserRequest, OidcUser> {

	private final RoleRepository roleRepository;

	public CustomOidcUserService(MemberRepository memberRepository,
		NicknameGenerator nicknameGenerator, RoleRepository roleRepository, FindRole findRole) {
		super(memberRepository, nicknameGenerator, roleRepository, findRole);
		this.roleRepository = roleRepository;
	}

	@Override
	@Transactional
	public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
		OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate = new DefaultOAuth2UserService();
		OAuth2User oAuth2User = delegate.loadUser(userRequest);
		OAuthAttribute attributes = getUserInfo(userRequest, oAuth2User);
		Member member = saveOrUpdate(attributes);
		return (OidcUser)createOAuth2User(member, userRequest, attributes.getSub());
	}

	@Override
	OAuth2User createOAuth2User(Member member, OAuth2UserRequest userRequest, String sub) {
		Collection<? extends GrantedAuthority> authorities = roleRepository.findAllById(member.getRoleIds()).stream()
			.map(Role::getRoleName)
			.map(SimpleGrantedAuthority::new)
			.toList();

		OidcIdToken idToken = ((OidcUserRequest)userRequest).getIdToken();
		Map<String, Object> claims = idToken.getClaims();
		Map<String, Object> memberAttribute = new HashMap<>();
		memberAttribute.put("id", member.getId());
		memberAttribute.putAll(member.getProfile().toMap());
		Set<String> roleNames = roleRepository.findAllById(member.getRoleIds()).stream()
			.map(Role::getRoleName)
			.collect(Collectors.toSet());
		memberAttribute.put("roles", roleNames);
		memberAttribute.putAll(claims);

		OidcUserInfo userInfo = new OidcUserInfo(memberAttribute);

		String nameAttributeKey = userRequest.getClientRegistration()
			.getProviderDetails()
			.getUserInfoEndpoint()
			.getUserNameAttributeName();
		memberAttribute.put(nameAttributeKey, sub);
		return new DefaultOidcUser(authorities, idToken, userInfo, nameAttributeKey);
	}
}
