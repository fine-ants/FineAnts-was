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
import co.fineants.api.global.init.properties.UserProperties;
import jakarta.validation.constraints.NotNull;

@Service
public class MemberSetupDataLoader {

	private final RoleRepository roleRepository;
	private final MemberRepository memberRepository;
	private final UserProperties userProperties;
	private final UserProperties managerProperties;
	private final UserProperties adminProperties;
	private final PasswordEncoder passwordEncoder;

	public MemberSetupDataLoader(RoleRepository roleRepository, MemberRepository memberRepository,
		UserProperties userProperties, UserProperties managerProperties, UserProperties adminProperties,
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
		// 기본 사용자 생성
		String email = userProperties.getEmail();
		String provider = "local";
		// 이미 존재하는 경우 생성하지 않음
		if (memberRepository.findMemberByEmailAndProvider(email, provider).isEmpty()) {
			// 멤버가 존재하지 않으면 새로 생성
			MemberProfile profile = MemberProfile.localMemberProfile(userProperties.getEmail(),
				userProperties.getNickname(), passwordEncoder.encode(userProperties.getPassword()),
				null);
			NotificationPreference notificationPreference = NotificationPreference.allActive();
			Member member = Member.createMember(profile, notificationPreference);
			memberRepository.save(member);
		}
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
