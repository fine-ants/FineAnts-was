package co.fineants.member.application;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import co.fineants.AbstractContainerBaseTest;
import co.fineants.member.domain.Member;
import co.fineants.member.domain.MemberRepository;
import co.fineants.member.presentation.dto.response.ProfileResponse;

class ReadMemberProfileTest extends AbstractContainerBaseTest {

	@Autowired
	private ReadMemberProfile readMemberProfile;

	@Autowired
	private MemberRepository memberRepository;

	@DisplayName("사용자는 프로필을 조회합니다.")
	@Test
	void readProfile() {
		// given
		Member member = memberRepository.save(createMember());

		// when
		ProfileResponse response = readMemberProfile.read(member.getId());

		// then
		ProfileResponse.NotificationPreferenceDto notificationPreferenceDto = ProfileResponse.NotificationPreferenceDto.builder()
			.browserNotify(true)
			.targetGainNotify(true)
			.maxLossNotify(true)
			.targetPriceNotify(true)
			.build();
		ProfileResponse.MemberProfileDto memberProfile = ProfileResponse.MemberProfileDto.builder()
			.id(member.getId())
			.nickname("nemo1234")
			.email("dragonbead95@naver.com")
			.profileUrl("profileUrl")
			.provider("local")
			.notificationPreferences(notificationPreferenceDto)
			.build();
		ProfileResponse profileResponse = new ProfileResponse(memberProfile);
		assertThat(response).isEqualTo(profileResponse);
	}

}
