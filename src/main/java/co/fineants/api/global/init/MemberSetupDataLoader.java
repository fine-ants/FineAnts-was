package co.fineants.api.global.init;

import java.util.function.Supplier;

import org.apache.logging.log4j.util.Strings;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.fineants.api.domain.member.domain.entity.Member;
import co.fineants.api.domain.member.domain.entity.MemberProfile;
import co.fineants.api.domain.member.domain.entity.NotificationPreference;
import co.fineants.api.domain.member.repository.MemberRepository;
import co.fineants.api.domain.member.repository.RoleRepository;
import co.fineants.api.domain.role.domain.Role;
import co.fineants.api.global.errors.exception.business.NotFoundException;
import co.fineants.api.global.errors.exception.business.RoleNotFoundException;
import co.fineants.api.global.init.properties.MemberProperties;

@Service
public class MemberSetupDataLoader {

	private final RoleRepository roleRepository;
	private final MemberRepository memberRepository;
	private final PasswordEncoder passwordEncoder;

	public MemberSetupDataLoader(RoleRepository roleRepository, MemberRepository memberRepository,
		PasswordEncoder passwordEncoder) {
		this.roleRepository = roleRepository;
		this.memberRepository = memberRepository;
		this.passwordEncoder = passwordEncoder;
	}

	@Transactional
	public void setupMembers(MemberProperties memberProperties) {
		for (MemberProperties.MemberAuthProperty properties : memberProperties.getProperties()) {
			Role role = roleRepository.findRoleByRoleName(properties.getRoleName())
				.orElseThrow((Supplier<NotFoundException>)() -> new RoleNotFoundException(Strings.EMPTY));
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
		MemberProfile profile = MemberProfile.localMemberProfile(properties.getEmail(),
			properties.getNickname(), passwordEncoder.encode(properties.getPassword()),
			null);
		NotificationPreference notificationPreference = NotificationPreference.allActive();
		return Member.createMember(profile, notificationPreference);
	}

}
