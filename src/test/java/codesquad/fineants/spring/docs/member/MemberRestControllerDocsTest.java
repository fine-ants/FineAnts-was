package codesquad.fineants.spring.docs.member;

import static org.hamcrest.Matchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.http.HttpMethod.*;
import static org.springframework.restdocs.headers.HeaderDocumentation.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.*;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.restdocs.snippet.Attributes;

import codesquad.fineants.domain.jwt.Jwt;
import codesquad.fineants.domain.member.Member;
import codesquad.fineants.spring.api.member.controller.MemberRestController;
import codesquad.fineants.spring.api.member.request.LoginRequest;
import codesquad.fineants.spring.api.member.request.OauthMemberLoginRequest;
import codesquad.fineants.spring.api.member.response.LoginResponse;
import codesquad.fineants.spring.api.member.response.OauthMemberLoginResponse;
import codesquad.fineants.spring.api.member.response.OauthMemberResponse;
import codesquad.fineants.spring.api.member.response.ProfileChangeResponse;
import codesquad.fineants.spring.api.member.service.MemberService;
import codesquad.fineants.spring.api.member.service.request.ProfileChangeServiceRequest;
import codesquad.fineants.spring.api.member.service.request.SignUpServiceRequest;
import codesquad.fineants.spring.api.member.service.response.SignUpServiceResponse;
import codesquad.fineants.spring.docs.RestDocsSupport;
import codesquad.fineants.spring.util.ObjectMapperUtil;

public class MemberRestControllerDocsTest extends RestDocsSupport {

	private final MemberService memberService = Mockito.mock(MemberService.class);

	@Override
	protected Object initController() {
		return new MemberRestController(memberService);
	}

	@DisplayName("회원 OAuth 로그인 API")
	@Test
	void login() throws Exception {
		// given
		Member member = createMember();
		String code = "1234";
		String redirectUrl = "http://localhost:5173/signin?provider=kakao";
		String state = "1234";
		String url = "/api/auth/{provider}/login";

		OauthMemberLoginResponse mockResponse = OauthMemberLoginResponse.builder()
			.jwt(Jwt.builder()
				.accessToken("accessToken")
				.refreshToken("refreshToken")
				.build())
			.user(OauthMemberResponse.from(member))
			.build();
		given(memberService.login(ArgumentMatchers.any(OauthMemberLoginRequest.class)))
			.willReturn(mockResponse);
		// when
		mockMvc.perform(RestDocumentationRequestBuilders.post(url, "kakao")
				.param("code", code)
				.param("redirectUrl", redirectUrl)
				.param("state", state))
			.andExpect(status().isOk())
			.andExpect(jsonPath("code").value(equalTo(200)))
			.andExpect(jsonPath("status").value(equalTo("OK")))
			.andExpect(jsonPath("message").value(equalTo("로그인에 성공하였습니다")))
			.andExpect(jsonPath("data.jwt.accessToken").value(equalTo("accessToken")))
			.andExpect(jsonPath("data.jwt.refreshToken").value(equalTo("refreshToken")))
			.andExpect(jsonPath("data.user.id").value(equalTo(member.getId().intValue())))
			.andExpect(jsonPath("data.user.nickname").value(equalTo(member.getNickname())))
			.andExpect(jsonPath("data.user.email").value(equalTo(member.getEmail())))
			.andExpect(jsonPath("data.user.profileUrl").value(equalTo(member.getProfileUrl())))
			.andDo(
				document(
					"member_oauth-login",
					preprocessRequest(prettyPrint()),
					preprocessResponse(prettyPrint()),
					pathParameters(
						parameterWithName("provider").description("플랫폼 이름")
							.attributes(
								Attributes.key("constraints").value(
									String.join(",",
										"kakao",
										"google",
										"naver"
									)
								))
					),
					requestParameters(
						parameterWithName("code").description("인가 코드"),
						parameterWithName("redirectUrl").description("리다이렉트 URL"),
						parameterWithName("state").description("state")
					),
					responseFields(
						fieldWithPath("code").type(JsonFieldType.NUMBER)
							.description("코드"),
						fieldWithPath("status").type(JsonFieldType.STRING)
							.description("상태"),
						fieldWithPath("message").type(JsonFieldType.STRING)
							.description("메시지"),
						fieldWithPath("data").type(JsonFieldType.OBJECT)
							.description("응답 데이터"),
						fieldWithPath("data.jwt").type(JsonFieldType.OBJECT)
							.description("Json Web Token"),
						fieldWithPath("data.jwt.accessToken").type(JsonFieldType.STRING)
							.description("액세스 토큰"),
						fieldWithPath("data.jwt.refreshToken").type(JsonFieldType.STRING)
							.description("리프레시 토큰"),
						fieldWithPath("data.user").type(JsonFieldType.OBJECT)
							.description("회원 정보"),
						fieldWithPath("data.user.id").type(JsonFieldType.NUMBER)
							.description("회원 등록번호"),
						fieldWithPath("data.user.nickname").type(JsonFieldType.STRING)
							.description("닉네임"),
						fieldWithPath("data.user.email").type(JsonFieldType.STRING)
							.description("이메일"),
						fieldWithPath("data.user.profileUrl").type(JsonFieldType.STRING)
							.description("프로필 URL")
					)
				)
			);
	}

	@DisplayName("회원 일반 로그인 API")
	@Test
	void loginByLocal() throws Exception {
		// given
		Member member = createMember();
		String url = "/api/auth/login";
		Map<String, Object> body = Map.of(
			"email", "dragonbead95@naver.com",
			"password", "nemo1234@"
		);

		LoginResponse mockResponse = LoginResponse.builder()
			.jwt(Jwt.builder()
				.accessToken("accessToken")
				.refreshToken("refreshToken")
				.build())
			.user(OauthMemberResponse.from(member))
			.build();
		given(memberService.login(ArgumentMatchers.any(LoginRequest.class)))
			.willReturn(mockResponse);

		// when & then
		mockMvc.perform(RestDocumentationRequestBuilders.post(url)
				.contentType(MediaType.APPLICATION_JSON)
				.content(ObjectMapperUtil.serialize(body)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("code").value(equalTo(200)))
			.andExpect(jsonPath("status").value(equalTo("OK")))
			.andExpect(jsonPath("message").value(equalTo("로그인에 성공하였습니다.")))
			.andExpect(jsonPath("data.jwt.accessToken").value(equalTo("accessToken")))
			.andExpect(jsonPath("data.jwt.refreshToken").value(equalTo("refreshToken")))
			.andExpect(jsonPath("data.user.id").value(equalTo(member.getId().intValue())))
			.andExpect(jsonPath("data.user.nickname").value(equalTo(member.getNickname())))
			.andExpect(jsonPath("data.user.email").value(equalTo(member.getEmail())))
			.andExpect(jsonPath("data.user.profileUrl").value(equalTo(member.getProfileUrl())))
			.andDo(
				document(
					"member-login",
					preprocessRequest(prettyPrint()),
					preprocessResponse(prettyPrint()),
					requestFields(
						fieldWithPath("email").type(JsonFieldType.STRING).description("이메일"),
						fieldWithPath("password").type(JsonFieldType.STRING).description("비밀번호")
					),
					responseFields(
						fieldWithPath("code").type(JsonFieldType.NUMBER)
							.description("코드"),
						fieldWithPath("status").type(JsonFieldType.STRING)
							.description("상태"),
						fieldWithPath("message").type(JsonFieldType.STRING)
							.description("메시지"),
						fieldWithPath("data").type(JsonFieldType.OBJECT)
							.description("응답 데이터"),
						fieldWithPath("data.jwt").type(JsonFieldType.OBJECT)
							.description("Json Web Token"),
						fieldWithPath("data.jwt.accessToken").type(JsonFieldType.STRING)
							.description("액세스 토큰"),
						fieldWithPath("data.jwt.refreshToken").type(JsonFieldType.STRING)
							.description("리프레시 토큰"),
						fieldWithPath("data.user").type(JsonFieldType.OBJECT)
							.description("회원 정보"),
						fieldWithPath("data.user.id").type(JsonFieldType.NUMBER)
							.description("회원 등록번호"),
						fieldWithPath("data.user.nickname").type(JsonFieldType.STRING)
							.description("닉네임"),
						fieldWithPath("data.user.email").type(JsonFieldType.STRING)
							.description("이메일"),
						fieldWithPath("data.user.profileUrl").type(JsonFieldType.STRING)
							.description("프로필 URL")
					)
				)
			);
	}

	@DisplayName("회원 로그아웃 API")
	@Test
	void logout() throws Exception {
		// given
		String url = "/api/auth/logout";
		Map<String, Object> body = Map.of(
			"refreshToken", "refreshToken"
		);

		// when & then
		mockMvc.perform(RestDocumentationRequestBuilders.post(url)
				.contentType(MediaType.APPLICATION_JSON)
				.content(ObjectMapperUtil.serialize(body))
				.header(HttpHeaders.AUTHORIZATION, "Bearer accessToken"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("code").value(equalTo(200)))
			.andExpect(jsonPath("status").value(equalTo("OK")))
			.andExpect(jsonPath("message").value(equalTo("로그아웃에 성공하였습니다")))
			.andExpect(jsonPath("data").value(equalTo(null)))
			.andDo(
				document(
					"member-logout",
					preprocessRequest(prettyPrint()),
					preprocessResponse(prettyPrint()),
					requestHeaders(
						headerWithName(HttpHeaders.AUTHORIZATION).description("액세스 토큰")
					),
					requestFields(
						fieldWithPath("refreshToken").type(JsonFieldType.STRING).description("리프레시 토큰")
					),
					responseFields(
						fieldWithPath("code").type(JsonFieldType.NUMBER)
							.description("코드"),
						fieldWithPath("status").type(JsonFieldType.STRING)
							.description("상태"),
						fieldWithPath("message").type(JsonFieldType.STRING)
							.description("메시지"),
						fieldWithPath("data").type(JsonFieldType.NULL)
							.description("응답 데이터")
					)
				)
			);
	}

	@DisplayName("회원 계정 삭제 API")
	@Test
	void deleteAccount() throws Exception {
		// given
		Map<String, Object> body = Map.of(
			"refreshToken", "refreshToken"
		);

		// when & then
		mockMvc.perform(delete("/api/account")
				.contentType(MediaType.APPLICATION_JSON)
				.content(ObjectMapperUtil.serialize(body))
				.header(HttpHeaders.AUTHORIZATION, "Bearer accessToken"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("code").value(equalTo(200)))
			.andExpect(jsonPath("status").value(equalTo("OK")))
			.andExpect(jsonPath("message").value(equalTo("계정이 삭제되었습니다")))
			.andDo(
				document(
					"member-delete",
					preprocessRequest(prettyPrint()),
					preprocessResponse(prettyPrint()),
					requestHeaders(
						headerWithName(HttpHeaders.AUTHORIZATION).description("액세스 토큰")
					),
					requestFields(
						fieldWithPath("refreshToken").type(JsonFieldType.STRING)
							.description("리프레시 토큰")
					),
					responseFields(
						fieldWithPath("code").type(JsonFieldType.NUMBER)
							.description("코드"),
						fieldWithPath("status").type(JsonFieldType.STRING)
							.description("상태"),
						fieldWithPath("message").type(JsonFieldType.STRING)
							.description("메시지"),
						fieldWithPath("data").type(JsonFieldType.NULL)
							.description("응답 데이터")
					)
				)
			);
	}

	@DisplayName("사용자 일반 회원가입 API")
	@Test
	void signup() throws Exception {
		// given
		given(memberService.signup(ArgumentMatchers.any(SignUpServiceRequest.class)))
			.willReturn(SignUpServiceResponse.from(createMember()));

		Map<String, Object> profileInformationMap = Map.of(
			"nickname", "일개미1234",
			"email", "dragonbead95@naver.com",
			"password", "nemo1234@",
			"passwordConfirm", "nemo1234@");
		String json = ObjectMapperUtil.serialize(profileInformationMap);
		MockMultipartFile signupData = new MockMultipartFile(
			"signupData",
			"signupData",
			MediaType.APPLICATION_JSON_VALUE,
			json.getBytes(StandardCharsets.UTF_8));

		// when & then
		mockMvc.perform(multipart(POST, "/api/auth/signup")
				.file((MockMultipartFile)createMockMultipartFile())
				.file(signupData))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("code").value(equalTo(201)))
			.andExpect(jsonPath("status").value(equalTo("Created")))
			.andExpect(jsonPath("message").value(equalTo("회원가입이 완료되었습니다")))
			.andDo(
				document(
					"member-signup",
					preprocessRequest(prettyPrint()),
					preprocessResponse(prettyPrint()),
					requestParts(
						partWithName("profileImageFile")
							.optional()
							.description("프로필 파일"),
						partWithName("signupData")
							.description("회원가입 정보")
					),
					responseFields(
						fieldWithPath("code").type(JsonFieldType.NUMBER)
							.description("코드"),
						fieldWithPath("status").type(JsonFieldType.STRING)
							.description("상태"),
						fieldWithPath("message").type(JsonFieldType.STRING)
							.description("메시지"),
						fieldWithPath("data").type(JsonFieldType.NULL)
							.description("응답 데이터")
					)
				)
			);
	}

	@DisplayName("회원 프로필 수정 API")
	@Test
	void changeProfile() throws Exception {
		// given
		Member member = createMember();

		Map<String, Object> profileInformationMap = Map.of("nickname", "일개미12345");
		MockMultipartFile profileInformation = new MockMultipartFile(
			"profileInformation",
			"profileInformation",
			MediaType.APPLICATION_JSON_VALUE,
			ObjectMapperUtil.serialize(profileInformationMap)
				.getBytes(StandardCharsets.UTF_8));

		member.updateNickname("일개미12345");
		given(memberService.changeProfile(ArgumentMatchers.any(ProfileChangeServiceRequest.class)))
			.willReturn(ProfileChangeResponse.from(member));
		// when & then
		mockMvc.perform(multipart(POST, "/api/profile")
				.file((MockMultipartFile)createMockMultipartFile())
				.file(profileInformation)
				.header(HttpHeaders.AUTHORIZATION, "Bearer accessToken"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("code").value(equalTo(200)))
			.andExpect(jsonPath("status").value(equalTo("OK")))
			.andExpect(jsonPath("message").value(equalTo("프로필이 수정되었습니다")))
			.andExpect(jsonPath("data.user.id").value(equalTo(1)))
			.andExpect(jsonPath("data.user.nickname").value(equalTo("일개미12345")))
			.andExpect(jsonPath("data.user.email").value(equalTo("kim1234@gmail.com")))
			.andExpect(jsonPath("data.user.profileUrl").value(equalTo("profileUrl")))
			.andDo(
				document(
					"member-update",
					preprocessRequest(prettyPrint()),
					preprocessResponse(prettyPrint()),
					requestHeaders(
						headerWithName(HttpHeaders.AUTHORIZATION).description("액세스 토큰")
					),
					requestParts(
						partWithName("profileImageFile")
							.optional()
							.description("프로필 파일"),
						partWithName("profileInformation")
							.optional()
							.description("회원 수정 정보")
					),
					responseFields(
						fieldWithPath("code").type(JsonFieldType.NUMBER)
							.description("코드"),
						fieldWithPath("status").type(JsonFieldType.STRING)
							.description("상태"),
						fieldWithPath("message").type(JsonFieldType.STRING)
							.description("메시지"),
						fieldWithPath("data").type(JsonFieldType.OBJECT)
							.description("응답 데이터"),
						fieldWithPath("data.user").type(JsonFieldType.OBJECT)
							.description("회원 정보"),
						fieldWithPath("data.user.id").type(JsonFieldType.NUMBER)
							.description("회원 등록번호"),
						fieldWithPath("data.user.nickname").type(JsonFieldType.STRING)
							.description("회원 닉네임"),
						fieldWithPath("data.user.email").type(JsonFieldType.STRING)
							.description("회원 이메일"),
						fieldWithPath("data.user.profileUrl").type(JsonFieldType.STRING)
							.description("회원 프로필 URL")
					)
				)
			);
	}
}