package co.fineants.api.global.init;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.fineants.api.domain.member.domain.entity.Member;
import co.fineants.api.domain.member.domain.entity.MemberProfile;
import co.fineants.api.domain.member.domain.entity.Nickname;
import co.fineants.api.domain.member.domain.entity.NotificationPreference;
import co.fineants.api.domain.member.repository.MemberRepository;
import co.fineants.api.domain.member.repository.RoleRepository;
import co.fineants.api.domain.member.service.factory.NicknameFactory;
import co.fineants.api.domain.role.domain.Role;
import co.fineants.api.global.errors.exception.business.RoleNotFoundException;
import co.fineants.api.global.init.properties.MemberProperties;

@Service
public class MemberSetupDataLoader {

	private final RoleRepository roleRepository;
	private final MemberRepository memberRepository;
	private final PasswordEncoder passwordEncoder;
	private final NicknameFactory nicknameFactory;

	public MemberSetupDataLoader(RoleRepository roleRepository, MemberRepository memberRepository,
		PasswordEncoder passwordEncoder, NicknameFactory nicknameFactory) {
		this.roleRepository = roleRepository;
		this.memberRepository = memberRepository;
		this.passwordEncoder = passwordEncoder;
		this.nicknameFactory = nicknameFactory;
	}

	@Transactional
	public void setupMembers(MemberProperties memberProperties) {
		for (MemberProperties.MemberAuthProperty properties : memberProperties.getProperties()) {
			Role role = roleRepository.findRoleByRoleName(properties.getRoleName())
				.orElseThrow(() -> new RoleNotFoundException(properties.getRoleName()));
			saveMember(properties, role);
		}
	}

	private void saveMember(MemberProperties.MemberAuthProperty properties, Role role) {
		String email = properties.getEmail();
		String provider = properties.getProvider();
		if (isEmptyMemberBy(email, provider)) {
			Member member = createMember(properties);
			member.addRoleId(role.getId());
			memberRepository.save(member);
		}
	}

	private boolean isEmptyMemberBy(String email, String provider) {
		return memberRepository.findMemberByEmailAndProvider(email, provider).isEmpty();
	}

	private Member createMember(MemberProperties.MemberAuthProperty properties) {
		Nickname nickname = nicknameFactory.create(properties.getNickname());
		MemberProfile profile = MemberProfile.localMemberProfile(properties.getEmail(), nickname,
			passwordEncoder.encode(properties.getPassword()), null);
		NotificationPreference notificationPreference = NotificationPreference.allActive();
		return Member.createMember(profile, notificationPreference);
	}

}
