package co.fineants.api.global.security.ajax.service;

import java.util.List;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.fineants.role.infrastructure.RoleRepository;
import co.fineants.api.global.security.ajax.provider.MemberContext;
import co.fineants.member.domain.Member;
import co.fineants.member.domain.MemberEmail;
import co.fineants.member.domain.MemberRepository;
import co.fineants.role.domain.Role;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service("memberUserDetailsService")
@RequiredArgsConstructor
@Slf4j
public class MemberUserDetailService implements UserDetailsService {

	private final MemberRepository memberRepository;
	private final RoleRepository roleRepository;

	@Override
	@Transactional(readOnly = true)
	public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
		MemberEmail memberEmail = new MemberEmail(email);
		Member member = memberRepository.findMemberByEmailAndProvider(memberEmail, "local")
			.orElseThrow(() -> new BadCredentialsException("invalid email"));
		log.debug("findMember : {}", member);

		List<SimpleGrantedAuthority> authorities = roleRepository.findAllById(member.getRoleIds()).stream()
			.map(Role::getRoleName)
			.map(SimpleGrantedAuthority::new)
			.toList();
		log.debug("fineMember's authorities : {}", authorities);
		return new MemberContext(member, authorities);
	}
}
