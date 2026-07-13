package co.fineants.api.global.security.oauth.service;

import static org.assertj.core.api.Assertions.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import co.fineants.AbstractContainerBaseTest;
import co.fineants.api.global.security.oauth.dto.OAuthAttribute;
import co.fineants.member.application.NicknameGenerator;
import co.fineants.member.domain.Member;
import co.fineants.member.domain.MemberRepository;
import co.fineants.role.application.FindRole;
import co.fineants.role.domain.RoleRepository;

class CustomOidcUserServiceTest extends AbstractContainerBaseTest {

	@Autowired
	private MemberRepository memberRepository;

	@Autowired
	private NicknameGenerator nicknameGenerator;

	@Autowired
	private RoleRepository roleRepository;

	@Autowired
	private FindRole findRole;

	@Transactional
	@DisplayName("구글 계정이 다른 프로필 사진으로 변경된 상태여도 회원 정보 저장시 프로필 사진을 유지해야 한다")
	@Test
	void givenGoogleProfilePictureChanged_whenSavingUserInfo_thenOriginalProfilePictureIsMaintained() {
		// given
		memberRepository.save(createOauthMember());
		AbstractUserService userService = new CustomOidcUserService(memberRepository,
			nicknameGenerator, roleRepository, findRole);
		Map<String, Object> attributes = new HashMap<>();
		attributes.put("email", "fineants1234@gmail.com");
		attributes.put("profile", "profileUrl0");
		attributes.put("sub", "123445");
		OAuthAttribute oAuthAttribute = OAuthAttribute.of("google", attributes, "sub");
		// when
		Member member = userService.saveOrUpdate(oAuthAttribute);
		// then
		assertThat(member.getProfileUrl().orElseThrow()).isEqualTo("profileUrl1");
	}

}
