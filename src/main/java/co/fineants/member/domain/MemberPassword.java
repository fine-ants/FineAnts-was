package co.fineants.member.domain;

import java.util.regex.Pattern;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode
public class MemberPassword {
	/**
	 * 비밀번호 유효성 검사를 위한 정규식 패턴입니다.
	 * <p>
	 * <b>정규식:</b> {@code ^(?=.*[a-zA-Z])(?=.*\\d)(?=.*[!@#$%^&*])[a-zA-Z\\d!@#$%^&*]{8,16}$}
	 * <p>
	 * <b>패턴 설명:</b>
	 * <ul>
	 *   <li><b>^</b> — 문자열의 시작을 의미합니다.</li>
	 *   <li><b>(?=.*[a-zA-Z])</b> — 최소 한 개 이상의 영문자(대문자 또는 소문자)가 포함되어야 합니다.</li>
	 *   <li><b>(?=.*\\d)</b> — 최소 한 개 이상의 숫자가 포함되어야 합니다.</li>
	 *   <li><b>(?=.*[!@#$%^&*])</b> — 최소 한 개 이상의 특수문자(!, @, #, $, %, ^, &, *)가 포함되어야 합니다.</li>
	 *   <li><b>[a-zA-Z\\d!@#$%^&*]{8,16}</b> — 영문자, 숫자, 지정된 특수문자만 사용 가능하며 전체 길이는 8~16자여야 합니다.</li>
	 *   <li><b>$</b> — 문자열의 끝을 의미합니다.</li>
	 * </ul>
	 *
	 * <b>유효한 비밀번호 조건 요약:</b>
	 * <ul>
	 *   <li>길이: 8자 이상 16자 이하</li>
	 *   <li>영문자(대/소문자) 최소 1개 포함</li>
	 *   <li>숫자 최소 1개 포함</li>
	 *   <li>특수문자(!@#$%^&*) 최소 1개 포함</li>
	 *   <li>공백, 이모지, 허용되지 않은 문자는 포함 불가</li>
	 * </ul>
	 */
	public static final String PASSWORD_REGEXP = "^(?=.*[a-zA-Z])(?=.*\\d)(?=.*[!@#$%^&*])[a-zA-Z\\d!@#$%^&*]{8,16}$";
	private static final Pattern PASSWORD_PATTERN = Pattern.compile(PASSWORD_REGEXP);

	@Column(name = "password")
	private String value;

	public MemberPassword(String rawPassword, MemberPasswordEncoder passwordEncoder) {
		if (!PASSWORD_PATTERN.matcher(rawPassword).matches()) {
			throw new IllegalArgumentException("Password format is invalid");
		}
		this.value = passwordEncoder.encode(rawPassword);
	}

}

