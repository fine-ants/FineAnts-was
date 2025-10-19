package co.fineants.member.domain;

import java.util.regex.Pattern;

import org.springframework.security.crypto.password.PasswordEncoder;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode
public class MemberPassword {
	/**
	 * <p>
	 * 비밀번호 정규식 패턴:<br>
	 * - ^                         : 문자열의 시작<br>
	 * - (?=.*[a-zA-Z])            : 적어도 하나 이상의 영문 대소문자 포함<br>
	 * - (?=.*[\\d])               : 적어도 하나 이상의 숫자 포함<br>
	 * - (?=.*[!@#$%^&*])          : 적어도 하나 이상의 특수문자(!@#$%^&*) 포함<br>
	 * - .{8,16}                   : 전체 길이가 8자 이상 16자 이하<br>
	 * - $                         : 문자열의 끝<br>
	 *<br>
	 * 즉, 영문 + 숫자 + 특수문자를 모두 포함하며 8~16자 사이여야 유효한 비밀번호로 인정됩니다.<br>
	 *<br>
	 * 예시 (유효): Abcd1234! / Hello@2025<br>
	 * 예시 (무효): abcdefgh (숫자, 특수문자 없음)<br>
	 * </p>
	 */
	public static final String PASSWORD_REGEXP = "^(?=.*[a-zA-Z])(?=.*\\d)(?=.*[!@#$%^&*])[a-zA-Z\\d!@#$%^&*]{8,16}$";
	private static final Pattern PASSWORD_PATTERN = Pattern.compile(PASSWORD_REGEXP);

	@Column(name = "password")
	private String value;

	public MemberPassword(String rawPassword, PasswordEncoder passwordEncoder) {
		if (!PASSWORD_PATTERN.matcher(rawPassword).matches()) {
			throw new IllegalArgumentException("Password format is invalid");
		}
		this.value = passwordEncoder.encode(rawPassword);
	}
}

