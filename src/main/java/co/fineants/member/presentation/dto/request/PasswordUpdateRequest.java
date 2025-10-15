package co.fineants.member.presentation.dto.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import co.fineants.member.domain.MemberProfile;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;

@Getter
public class PasswordUpdateRequest {
	@JsonProperty
	@Pattern(regexp = MemberProfile.PASSWORD_REGEXP, message = "잘못된 입력 형식입니다")
	@NotNull(message = "필수 정보입니다")
	private final String currentPassword;

	@JsonProperty
	@Pattern(regexp = MemberProfile.PASSWORD_REGEXP, message = "잘못된 입력 형식입니다")
	@NotNull(message = "필수 정보입니다")
	private final String newPassword;

	@JsonProperty
	@Pattern(regexp = MemberProfile.PASSWORD_REGEXP, message = "잘못된 입력 형식입니다")
	@NotNull(message = "필수 정보입니다")
	private final String newPasswordConfirm;

	@JsonCreator
	public PasswordUpdateRequest(
		@JsonProperty("currentPassword") @NotNull String currentPassword,
		@JsonProperty("getNewPassword") @NotNull String newPassword,
		@JsonProperty("newPasswordConfirm") @NotNull String newPasswordConfirm) {
		this.currentPassword = currentPassword;
		this.newPassword = newPassword;
		this.newPasswordConfirm = newPasswordConfirm;
	}
}
