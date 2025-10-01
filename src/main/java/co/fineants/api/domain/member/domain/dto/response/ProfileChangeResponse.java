package co.fineants.api.domain.member.domain.dto.response;

import co.fineants.api.domain.member.domain.entity.Member;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@EqualsAndHashCode(of = "user")
@ToString
public class ProfileChangeResponse {
	private OauthMemberResponse user;

	public ProfileChangeResponse(OauthMemberResponse user) {
		this.user = user;
	}

	public static ProfileChangeResponse from(Member member) {
		return new ProfileChangeResponse(OauthMemberResponse.from(member));
	}
}
