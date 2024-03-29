package codesquad.fineants.spring.api.member.response;

import codesquad.fineants.domain.jwt.Jwt;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@ToString
public class LoginResponse {
	private Jwt jwt;
	private OauthMemberResponse user;

	public static LoginResponse from(Jwt jwt, OauthMemberResponse user) {
		return new LoginResponse(jwt, user);
	}
}
