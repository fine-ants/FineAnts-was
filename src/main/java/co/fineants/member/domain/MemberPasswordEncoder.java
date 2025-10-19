package co.fineants.member.domain;

public interface MemberPasswordEncoder {
	String encode(String rawPassword);

	boolean matches(String rawPassword, String encodedPassword);
}
