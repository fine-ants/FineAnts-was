package co.fineants.member.presentation.dto.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import co.fineants.member.domain.MemberEmail;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode
public class VerifyEmailRequest {
	@NotBlank(message = "이메일은 필수 정보입니다")
	@Pattern(regexp = MemberEmail.EMAIL_REGEXP, message = "잘못된 입력 형식입니다")
	@JsonProperty
	private final String email;

	@JsonCreator
	public VerifyEmailRequest(@JsonProperty("email") String email) {
		this.email = email;
	}
}
