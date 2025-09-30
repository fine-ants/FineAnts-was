package co.fineants.api.global.init;

import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

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
import jakarta.validation.constraints.NotNull;

@Service
public class MemberSetupDataLoader {

	private final RoleRepository roleRepository;
	private final MemberRepository memberRepository;
	private final PasswordEncoder passwordEncoder;
	private final MemberProperties memberProperties;

	public MemberSetupDataLoader(RoleRepository roleRepository, MemberRepository memberRepository,
		PasswordEncoder passwordEncoder, MemberProperties memberProperties) {
		this.roleRepository = roleRepository;
		this.memberRepository = memberRepository;
		this.passwordEncoder = passwordEncoder;
		this.memberProperties = memberProperties;
	}

	@Transactional
	public void setupMembers() {
		for (MemberProperties.MemberAuthProperty properties : memberProperties.getProperties()) {
			Role role = roleRepository.findRoleByRoleName(properties.getRoleName())
				.orElseThrow(supplierNotFoundRoleException());
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

	@NotNull
	private static Supplier<NotFoundException> supplierNotFoundRoleException() {
		return () -> new RoleNotFoundException(Strings.EMPTY);
	}

	private void createMemberIfNotFound(String email, String nickname, String password,
		Set<Role> roleSet) {
		Member member = findOrCreateNewMember(email, nickname, password, roleSet);
		memberRepository.save(member);
	}

	private Member findOrCreateNewMember(String email, String nickname, String password, Set<Role> roleSet) {
		return memberRepository.findMemberByEmailAndProvider(email, "local")
			.orElseGet(supplierNewMember(email, nickname, password, roleSet));
	}

	@NotNull
	private Supplier<Member> supplierNewMember(String email, String nickname, String password, Set<Role> roleSet) {
		return () -> {
			MemberProfile profile = MemberProfile.localMemberProfile(email, nickname, passwordEncoder.encode(password),
				null);
			NotificationPreference notificationPreference = NotificationPreference.allActive();
			Member newMember = Member.createMember(profile, notificationPreference);
			Set<Long> roleIds = roleSet.stream()
				.map(Role::getId)
				.collect(Collectors.toSet());
			newMember.addRoleIds(roleIds);
			return newMember;
		};
	}
}
