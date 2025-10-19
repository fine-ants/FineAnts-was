package co.fineants.member.presentation;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import co.fineants.api.global.api.ApiResponse;
import co.fineants.api.global.success.MemberSuccessCode;
import co.fineants.member.application.MemberNotificationService;
import co.fineants.member.application.UpdateNotificationPreference;
import co.fineants.member.presentation.dto.request.MemberNotificationAllDeleteRequest;
import co.fineants.member.presentation.dto.request.MemberNotificationAllReadRequest;
import co.fineants.member.presentation.dto.request.MemberNotificationPreferenceRequest;
import co.fineants.member.presentation.dto.response.MemberNotificationPreferenceResponse;
import co.fineants.member.presentation.dto.response.MemberNotificationResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/members/{memberId}")
@RequiredArgsConstructor
public class MemberNotificationRestController {

	private final MemberNotificationService notificationService;
	private final UpdateNotificationPreference preferenceService;

	// 회원의 알림 목록 조회
	@GetMapping("/notifications")
	public ApiResponse<MemberNotificationResponse> fetchNotifications(@PathVariable Long memberId) {
		return ApiResponse.success(MemberSuccessCode.OK_READ_NOTIFICATIONS,
			notificationService.searchMemberNotifications(memberId));
	}

	// 회원 알림 설정 수정
	@PutMapping("/notification/settings")
	public ApiResponse<Void> updateNotificationPreference(
		@PathVariable Long memberId,
		@Valid @RequestBody MemberNotificationPreferenceRequest request) {
		MemberNotificationPreferenceResponse response = preferenceService.update(memberId,
			request);
		log.info("회원 알림 설정 수정 처리 결과 : memberId={}, response={}", memberId, response);
		return ApiResponse.success(MemberSuccessCode.OK_UPDATE_NOTIFICATION_PREFERENCE);
	}

	// 회원 전체 알림 제거
	@DeleteMapping("/notifications")
	public ApiResponse<Void> deleteAllNotifications(
		@PathVariable Long memberId,
		@Valid @RequestBody MemberNotificationAllDeleteRequest request) {
		List<Long> deletedNotificationIds = notificationService.deleteMemberNotifications(memberId,
			request.notificationIds());
		log.info("회원 알림 모두 삭제 처리 결과 : memberId={}, 삭제한 알림 등록 번호={}", memberId, deletedNotificationIds);
		return ApiResponse.success(MemberSuccessCode.OK_DELETED_ALL_NOTIFICATIONS);
	}

	// 회원 특정 알림 제거
	@DeleteMapping("/notifications/{notificationId}")
	public ApiResponse<Void> deleteNotification(
		@PathVariable Long memberId,
		@PathVariable Long notificationId) {
		List<Long> deletedNotificationIds = notificationService.deleteMemberNotifications(
			memberId,
			List.of(notificationId)
		);
		log.info("회원 알림 모두 삭제 처리 결과 : memberId={}, 삭제한 알림 등록 번호={}", memberId, deletedNotificationIds);
		return ApiResponse.success(MemberSuccessCode.OK_DELETED_NOTIFICATION);
	}

	// 회원의 알림 모두 읽기
	@PatchMapping("/notifications")
	public ApiResponse<Void> readAllNotifications(
		@PathVariable Long memberId,
		@Valid @RequestBody MemberNotificationAllReadRequest request) {
		List<Long> notificationIds = notificationService.fetchMemberNotifications(memberId, request.notificationIds());
		log.info("회원 알림 모두 읽기 처리 결과 : memberId={}, 읽은 알림 등록 번호={}", memberId, notificationIds);
		return ApiResponse.success(MemberSuccessCode.OK_FETCH_ALL_NOTIFICATIONS);
	}
}
