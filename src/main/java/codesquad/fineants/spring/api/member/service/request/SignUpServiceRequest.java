package codesquad.fineants.spring.api.member.service.request;

import org.springframework.web.multipart.MultipartFile;

import codesquad.fineants.domain.member.Member;
import codesquad.fineants.spring.api.member.request.SignUpRequest;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
public class SignUpServiceRequest {
	private String nickname;
	private String email;
	private String password;
	private String passwordConfirm;
	private MultipartFile profileImageFile;

	public static SignUpServiceRequest create(String nickname, String email, String password, String passwordConfirm,
		MultipartFile profileImageFile) {
		return SignUpServiceRequest.builder()
			.nickname(nickname)
			.email(email)
			.password(password)
			.passwordConfirm(passwordConfirm)
			.profileImageFile(profileImageFile)
			.build();
	}

	public static SignUpServiceRequest of(SignUpRequest request, MultipartFile profileImageFile) {
		return SignUpServiceRequest.builder()
			.nickname(request.getNickname())
			.email(request.getEmail())
			.password(request.getPassword())
			.passwordConfirm(request.getPasswordConfirm())
			.profileImageFile(profileImageFile)
			.build();
	}

	public Member toEntity(String profileUrl, String encodedPassword) {
		return Member.builder()
			.email(email)
			.nickname(nickname)
			.profileUrl(profileUrl)
			.password(encodedPassword)
			.provider("local")
			.build();
	}
}
