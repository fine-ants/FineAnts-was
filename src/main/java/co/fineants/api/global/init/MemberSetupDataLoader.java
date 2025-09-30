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
import co.fineants.api.global.init.properties.AdminProperties;
import co.fineants.api.global.init.properties.ManagerProperties;
import co.fineants.api.global.init.properties.UserProperties;
import jakarta.validation.constraints.NotNull;

@Service
public class MemberSetupDataLoader {

	private final RoleRepository roleRepository;
	private final MemberRepository memberRepository;
	private final UserProperties userProperties;
	private final ManagerProperties managerProperties;
	private final AdminProperties adminProperties;
	private final PasswordEncoder passwordEncoder;

	public MemberSetupDataLoader(RoleRepository roleRepository, MemberRepository memberRepository,
		UserProperties userProperties, ManagerProperties managerProperties, AdminProperties adminProperties,
		PasswordEncoder passwordEncoder) {
		this.roleRepository = roleRepository;
		this.memberRepository = memberRepository;
		this.userProperties = userProperties;
		this.managerProperties = managerProperties;
		this.adminProperties = adminProperties;
		this.passwordEncoder = passwordEncoder;
	}

	@Transactional
	public void setupMembers() {
		Role userRole = roleRepository.findRoleByRoleName("ROLE_USER")
			.orElseThrow(supplierNotFoundRoleException());
		saveUserRoleMember(userProperties, userRole);

		Role managerRole = roleRepository.findRoleByRoleName("ROLE_MANAGER")
			.orElseThrow(supplierNotFoundRoleException());
		saveManagerRoleMember(managerProperties, managerRole);
	}

	private void saveUserRoleMember(UserProperties properties, Role role) {
		String email = properties.getEmail();
		String provider = "local";
		if (isEmptyMemberBy(email, provider)) {
			Member member = createMember(properties);
			member.addRoleId(role.getId());
			memberRepository.save(member);
		}
	}

	private boolean isEmptyMemberBy(String email, String provider) {
		return memberRepository.findMemberByEmailAndProvider(email, provider).isEmpty();
	}

	private Member createMember(UserProperties properties) {
		MemberProfile profile = MemberProfile.localMemberProfile(properties.getEmail(),
			properties.getNickname(), passwordEncoder.encode(properties.getPassword()),
			null);
		NotificationPreference notificationPreference = NotificationPreference.allActive();
		return Member.createMember(profile, notificationPreference);
	}

	private void saveManagerRoleMember(ManagerProperties properties, Role role) {
		String email = properties.getEmail();
		String provider = "local";
		if (isEmptyMemberBy(email, provider)) {
			Member member = createMember(properties);
			member.addRoleId(role.getId());
			memberRepository.save(member);
		}
	}

	private Member createMember(ManagerProperties properties) {
		MemberProfile profile = MemberProfile.localMemberProfile(properties.getEmail(),
			properties.getNickname(), passwordEncoder.encode(properties.getPassword()),
			null);
		NotificationPreference notificationPreference = NotificationPreference.allActive();
		return Member.createMember(profile, notificationPreference);
	}

	private void setupMemberResources() {
		Role userRole = roleRepository.findRoleByRoleName("ROLE_USER")
			.orElseThrow(supplierNotFoundRoleException());
		Role managerRole = roleRepository.findRoleByRoleName("ROLE_MANAGER")
			.orElseThrow(supplierNotFoundRoleException());
		Role adminRole = roleRepository.findRoleByRoleName("ROLE_ADMIN")
			.orElseThrow(supplierNotFoundRoleException());

		createMemberIfNotFound(
			userProperties.getEmail(),
			userProperties.getNickname(),
			userProperties.getPassword(),
			Set.of(userRole));
		createMemberIfNotFound(
			adminProperties.getEmail(),
			adminProperties.getNickname(),
			adminProperties.getPassword(),
			Set.of(adminRole));
		createMemberIfNotFound(
			managerProperties.getEmail(),
			managerProperties.getNickname(),
			managerProperties.getPassword(),
			Set.of(managerRole));
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
