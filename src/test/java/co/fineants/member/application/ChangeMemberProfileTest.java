package co.fineants.member.application;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.multipart.MultipartFile;

import co.fineants.AbstractContainerBaseTest;
import co.fineants.api.global.errors.exception.business.MemberProfileNotChangeException;
import co.fineants.api.global.errors.exception.business.NicknameDuplicateException;
import co.fineants.member.domain.Member;
import co.fineants.member.domain.MemberRepository;
import co.fineants.member.presentation.dto.request.ProfileChangeServiceRequest;
import co.fineants.member.presentation.dto.response.ProfileChangeResponse;

class ChangeMemberProfileTest extends AbstractContainerBaseTest {

	@Autowired
	private MemberRepository memberRepository;

	@Autowired
	private ChangeMemberProfile changeMemberProfile;

	@DisplayName("프로필 이미지와 닉네임이 주어진 상태에서 사용자의 프로필 정보를 변경한다")
	@ParameterizedTest
	@MethodSource(value = "co.fineants.TestDataProvider#validChangeProfileSource")
	void givenProfileImageFileAndNickname_whenChangeProfile_thenChangedProfileInfo(
		MultipartFile profileImageFile,
		String nickname,
		String expectedNickname) {
		// given
		Member member = memberRepository.save(createMember());
		ProfileChangeServiceRequest serviceRequest = ProfileChangeServiceRequest.of(
			profileImageFile,
			nickname,
			member.getId()
		);
		// when
		ProfileChangeResponse response = changeMemberProfile.changeProfile(serviceRequest);

		// then
		assertThat(response)
			.extracting("user")
			.extracting("nickname")
			.isEqualTo(expectedNickname);
	}

	@DisplayName("사용자는 회원 프로필에서 닉네임 변경시 중복되어 변경하지 못한다")
	@Test
	void changeProfile_whenDuplicateNickname_thenThrowException() {
		// given
		memberRepository.save(createMember("nemo12345"));
		Member member = memberRepository.save(createMember());
		ProfileChangeServiceRequest serviceRequest = ProfileChangeServiceRequest.of(
			null,
			"nemo12345",
			member.getId()
		);

		// when
		Throwable throwable = catchThrowable(() -> changeMemberProfile.changeProfile(serviceRequest));

		// then
		String expected = "nemo12345";
		assertThat(throwable)
			.isInstanceOf(NicknameDuplicateException.class)
			.hasMessage(expected);
	}

	@DisplayName("사용자는 회원 프로필에서 변경할 정보가 없어서 실패한다")
	@Test
	void changeProfile_whenNoChangeProfile_thenThrowException() {
		// given
		Member member = memberRepository.save(createMember());
		ProfileChangeServiceRequest serviceRequest = ProfileChangeServiceRequest.of(
			null,
			null,
			member.getId()
		);

		// when
		Throwable throwable = catchThrowable(() -> changeMemberProfile.changeProfile(serviceRequest));

		// then
		assertThat(throwable)
			.isInstanceOf(MemberProfileNotChangeException.class)
			.hasMessage(serviceRequest.toString());
	}
}
