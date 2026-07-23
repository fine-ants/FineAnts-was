package co.fineants.api.global.security.oauth.service;

import static org.assertj.core.api.Assertions.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.BDDMockito;

import co.fineants.TestDataFactory;
import co.fineants.api.global.security.oauth.dto.OAuthAttribute;
import co.fineants.member.application.NicknameGenerator;
import co.fineants.member.domain.Member;
import co.fineants.member.domain.MemberEmail;
import co.fineants.member.domain.MemberRepository;
import co.fineants.role.application.FindRole;
import co.fineants.role.domain.Role;
import co.fineants.role.domain.RoleRepository;

class CustomOidcUserServiceTest {
	private AbstractUserService userService;
	private MemberRepository memberRepository;
	private FindRole findRole;
	private RoleRepository roleRepository;

	@BeforeEach
	void setUp() {
		memberRepository = BDDMockito.mock(MemberRepository.class);
		NicknameGenerator nicknameGenerator = BDDMockito.mock(NicknameGenerator.class);
		roleRepository = BDDMockito.mock(RoleRepository.class);
		findRole = BDDMockito.mock(FindRole.class);
		userService = new CustomOidcUserService(memberRepository, nicknameGenerator, roleRepository,
			findRole);
	}

	@DisplayName("구글 계정이 다른 프로필 사진으로 변경된 상태여도 회원 정보 저장시 프로필 사진을 유지해야 한다")
	@Test
	void givenGoogleProfilePictureChanged_whenSavingUserInfo_thenOriginalProfilePictureIsMaintained() {
		// given
		String changeProfile = "changedProfileUrl";
		String provider = "google";
		String email = "nemo1234@gmail.com";
		String sub = "12345";

		Map<String, Object> attributes = new HashMap<>();
		attributes.put("email", email);
		attributes.put("profile", changeProfile);
		attributes.put("sub", sub);
		OAuthAttribute oAuthAttribute = OAuthAttribute.of(provider, attributes, "sub");

		Role role = new Role(1L, "ROLE_USER", "회원");
		BDDMockito.given(findRole.findBy("ROLE_USER"))
			.willReturn(role);
		MemberEmail memberEmail = new MemberEmail(email);
		Member member = TestDataFactory.createOauthMember();
		BDDMockito.given(memberRepository.findMemberByEmailAndProvider(memberEmail, provider))
			.willReturn(Optional.of(member));
		BDDMockito.given(roleRepository.findAllById(member.getRoleIds()))
			.willReturn(Set.of(role));
		Set<String> roleNames = Set.of(role.getRoleName());
		BDDMockito.given(roleRepository.findRolesByRoleNames(roleNames))
			.willReturn(Set.of(role));
		BDDMockito.given(memberRepository.save(ArgumentMatchers.any(Member.class)))
			.willReturn(member);

		// when
		Member actualMember = userService.saveOrUpdate(oAuthAttribute);
		// then
		assertThat(actualMember.getProfileUrl().orElseThrow()).isEqualTo("profileUrl");
		assertThat(actualMember.getProfileUrl().orElseThrow()).isNotEqualTo(changeProfile);
	}

}
