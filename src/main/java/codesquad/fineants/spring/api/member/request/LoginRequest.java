package codesquad.fineants.spring.api.member.request;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class LoginRequest {
	private String email;
	private String password;

	public static LoginRequest create(String email, String password) {
		return new LoginRequest(email, password);
	}
}
