package co.fineants.api.global.init;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.fineants.api.global.errors.exception.business.RoleNotFoundException;
import co.fineants.api.global.init.properties.MemberProperties;
import co.fineants.member.domain.Member;
import co.fineants.member.domain.MemberEmail;
import co.fineants.member.domain.MemberPassword;
import co.fineants.member.domain.MemberPasswordEncoder;
import co.fineants.member.domain.MemberProfile;
import co.fineants.member.domain.MemberRepository;
import co.fineants.member.domain.Nickname;
import co.fineants.member.domain.NotificationPreference;
import co.fineants.role.domain.Role;
import co.fineants.role.domain.RoleRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MemberSetupDataLoader {

	private final RoleRepository roleRepository;
	private final MemberRepository memberRepository;
	private final MemberPasswordEncoder memberPasswordEncoder;

	@Transactional
	public void setupMembers(MemberProperties memberProperties) {
		for (MemberProperties.MemberAuthProperty properties : memberProperties.getProperties()) {
			Role role = roleRepository.findRoleByRoleName(properties.getRoleName())
				.orElseThrow(() -> new RoleNotFoundException(properties.getRoleName()));
			saveMember(properties, role);
		}
	}

	private void saveMember(MemberProperties.MemberAuthProperty properties, Role role) {
		MemberEmail email = new MemberEmail(properties.getEmail());
		String provider = properties.getProvider();
		if (isEmptyMemberBy(email, provider)) {
			Member member = createMember(properties);
			member.addRoleId(role.getId());
			memberRepository.save(member);
		}
	}

	private boolean isEmptyMemberBy(MemberEmail email, String provider) {
		return memberRepository.findMemberByEmailAndProvider(email, provider).isEmpty();
	}

	private Member createMember(MemberProperties.MemberAuthProperty properties) {
		MemberEmail memberEmail = new MemberEmail(properties.getEmail());
		Nickname nickname = new Nickname(properties.getNickname());
		MemberPassword memberPassword = new MemberPassword(properties.getPassword(), memberPasswordEncoder);
		String profileUrl = null;
		MemberProfile profile = MemberProfile.localMemberProfile(memberEmail, nickname, memberPassword, profileUrl);
		NotificationPreference notificationPreference = NotificationPreference.allActive();
		return Member.createMember(profile, notificationPreference);
	}

}
