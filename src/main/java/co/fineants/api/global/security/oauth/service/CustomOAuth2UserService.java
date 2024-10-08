package co.fineants.api.global.security.oauth.service;

import java.util.Collection;
import java.util.Map;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.fineants.api.domain.member.domain.entity.Member;
import co.fineants.api.domain.member.repository.MemberRepository;
import co.fineants.api.domain.member.repository.RoleRepository;
import co.fineants.api.domain.member.service.NicknameGenerator;
import co.fineants.api.domain.notificationpreference.repository.NotificationPreferenceRepository;
import co.fineants.api.global.security.oauth.dto.OAuthAttribute;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class CustomOAuth2UserService extends AbstractUserService
	implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

	public CustomOAuth2UserService(MemberRepository memberRepository,
		NotificationPreferenceRepository notificationPreferenceRepository,
		NicknameGenerator nicknameGenerator, RoleRepository roleRepository) {
		super(memberRepository, notificationPreferenceRepository, nicknameGenerator, roleRepository);
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
		Collection<? extends GrantedAuthority> authorities = member.getSimpleGrantedAuthorities();
		Map<String, Object> memberAttribute = member.toAttributeMap();
		String nameAttributeKey = userRequest.getClientRegistration()
			.getProviderDetails()
			.getUserInfoEndpoint()
			.getUserNameAttributeName();
		memberAttribute.put(nameAttributeKey, sub);
		return new DefaultOAuth2User(authorities, memberAttribute, nameAttributeKey);
	}
}
