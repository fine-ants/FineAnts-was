package co.fineants.api.global.security.oauth.service;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.fineants.role.infrastructure.RoleRepository;
import co.fineants.api.domain.member.service.NicknameGenerator;
import co.fineants.api.global.security.oauth.dto.OAuthAttribute;
import co.fineants.member.domain.Member;
import co.fineants.member.domain.MemberRepository;
import co.fineants.role.domain.Role;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class CustomOAuth2UserService extends AbstractUserService
	implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

	private final RoleRepository roleRepository;

	public CustomOAuth2UserService(MemberRepository memberRepository,
		NicknameGenerator nicknameGenerator, RoleRepository roleRepository) {
		super(memberRepository, nicknameGenerator, roleRepository);
		this.roleRepository = roleRepository;
	}

	@Override
	@Transactional
	public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
		OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate = new DefaultOAuth2UserService();
		OAuth2User oAuth2User = delegate.loadUser(userRequest);
		OAuthAttribute attributes = getUserInfo(userRequest, oAuth2User);
		Member member = saveOrUpdate(attributes);
		return createOAuth2User(member, userRequest, attributes.getSub());
	}

	@Override
	OAuth2User createOAuth2User(Member member, OAuth2UserRequest userRequest, String sub) {
		Collection<? extends GrantedAuthority> authorities = roleRepository.findAllById(member.getRoleIds()).stream()
			.map(Role::getRoleName)
			.map(SimpleGrantedAuthority::new)
			.toList();
		Map<String, Object> memberAttribute = new HashMap<>();
		memberAttribute.put("id", member.getId());
		memberAttribute.putAll(member.getProfile().toMap());
		Set<String> roleNames = roleRepository.findAllById(member.getRoleIds()).stream()
			.map(Role::getRoleName)
			.collect(Collectors.toSet());
		memberAttribute.put("roles", roleNames);
		String nameAttributeKey = userRequest.getClientRegistration()
			.getProviderDetails()
			.getUserInfoEndpoint()
			.getUserNameAttributeName();
		memberAttribute.put(nameAttributeKey, sub);

		return new DefaultOAuth2User(authorities, memberAttribute, nameAttributeKey);
	}
}
