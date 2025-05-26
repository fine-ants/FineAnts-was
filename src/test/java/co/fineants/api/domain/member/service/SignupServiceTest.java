package co.fineants.api.domain.member.service;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;

import co.fineants.AbstractContainerBaseTest;
import co.fineants.api.domain.member.domain.entity.Member;
import co.fineants.api.domain.member.domain.entity.MemberProfile;
import co.fineants.api.domain.member.repository.MemberRepository;
import co.fineants.api.domain.member.repository.MemberRoleRepository;
import co.fineants.api.domain.notificationpreference.repository.NotificationPreferenceRepository;
import co.fineants.api.global.errors.exception.business.EmailInvalidInputException;
import co.fineants.api.global.errors.exception.business.NicknameDuplicateException;
import co.fineants.api.global.errors.exception.business.NicknameInvalidInputException;

class SignupServiceTest extends AbstractContainerBaseTest {

	@Autowired
	private SignupService service;

	@Autowired
	private MemberRepository memberRepository;

	@Autowired
	private MemberRoleRepository memberRoleRepository;

	@Autowired
	private NotificationPreferenceRepository notificationPreferenceRepository;

	@DisplayName("사용자는 회원가입시 회원 정보를 저장한다")
	@Test
	void should_saveMember_whenSignup() {
		// given
		MemberProfile profile = MemberProfile.localMemberProfile("ants1@gmail.com", "ants1", "ants1234@", null);
		Member member = Member.localMember(profile);
		// when
		service.signup(member);
		// then
		int memberSize = memberRepository.findAll().size();
		assertThat(memberSize).isEqualTo(1);

		int memberRoleSize = memberRoleRepository.findAll().size();
		assertThat(memberRoleSize).isEqualTo(1);

		int preferenceSize = notificationPreferenceRepository.findAll().size();
		assertThat(preferenceSize).isEqualTo(1);
	}

	@DisplayName("사용자는 유효하지 않은 형식의 이메일이 주어졌을때 회원가입에 실패한다")
	@ParameterizedTest
	@MethodSource(value = "invalidEmailSource")
	void givenInvalidEmail_whenValidateEmail_thenFailSignup(String email) {
		// given
		MemberProfile profile = MemberProfile.localMemberProfile(email, "ants1", "ants1234@", null);
		Member member = Member.localMember(profile);
		// when
		Throwable throwable = catchThrowable(() -> service.signup(member));
		// then
		assertThat(throwable)
			.isInstanceOf(EmailInvalidInputException.class);
	}

	@DisplayName("사용자는 유효하지 않은 형식의 닉네임이 주어졌을때 회원가입에 실패한다")
	@ParameterizedTest
	@MethodSource(value = "invalidNicknameSource")
	void givenInvalidNickname_whenValidateNickname_thenFailSignup(String nickname) {
		// given
		MemberProfile profile = MemberProfile.localMemberProfile("ants1234@gmail.com", nickname, "ants1234@", null);
		Member member = Member.localMember(profile);
		// when
		Throwable throwable = catchThrowable(() -> service.signup(member));
		// then
		assertThat(throwable)
			.isInstanceOf(NicknameInvalidInputException.class);
	}

	@DisplayName("사용자는 이미 존재하는 닉네임을 가지고 회원가입 할 수 없다.")
	@Test
	void givenDuplicatedNickname_whenValidateNickname_thenFailSignup() {
		// given
		String nickname = "ants1";
		MemberProfile profile = MemberProfile.localMemberProfile("ants1234@gmail.com", nickname, "ants1234@", null);
		Member member = Member.localMember(profile);
		memberRepository.save(member);

		MemberProfile otherProfile = MemberProfile.localMemberProfile("ants4567@gmail.com", nickname, "ants4567@",
			null);
		Member otherMember = Member.localMember(otherProfile);
		// when
		Throwable throwable = catchThrowable(() -> service.signup(otherMember));
		// then
		assertThat(throwable)
			.isInstanceOf(NicknameDuplicateException.class);
	}

}
