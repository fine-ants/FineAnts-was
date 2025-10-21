package co.fineants.member.infrastructure;

import org.springframework.security.crypto.password.PasswordEncoder;

import co.fineants.member.domain.MemberPasswordEncoder;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class SpringMemberPasswordEncoder implements MemberPasswordEncoder {
	private final PasswordEncoder passwordEncoder;

	@Override
	public String encode(String rawPassword) {
		return passwordEncoder.encode(rawPassword);
	}

	@Override
	public boolean matches(String rawPassword, String encodedPassword) {
		return passwordEncoder.matches(rawPassword, encodedPassword);
	}
}
