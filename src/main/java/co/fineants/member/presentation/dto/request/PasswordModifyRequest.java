package co.fineants.member.presentation.dto.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotNull;

public class PasswordModifyRequest {
	@JsonProperty
	@NotNull(message = "필수 정보입니다")
	private final String currentPassword;
	@JsonProperty
	@NotNull(message = "필수 정보입니다")
	private final String newPassword;
	@JsonProperty
	@NotNull(message = "필수 정보입니다")
	private final String newPasswordConfirm;

	@JsonCreator
	public PasswordModifyRequest(
		@JsonProperty("currentPassword") @NotNull String currentPassword,
		@JsonProperty("newPassword") @NotNull String newPassword,
		@JsonProperty("newPasswordConfirm") @NotNull String newPasswordConfirm) {
		this.currentPassword = currentPassword;
		this.newPassword = newPassword;
		this.newPasswordConfirm = newPasswordConfirm;
	}

	public String currentPassword() {
		return currentPassword;
	}

	public String newPassword() {
		return newPassword;
	}

	public boolean matchPassword() {
		return newPassword.equals(newPasswordConfirm);
	}
}
