package co.fineants.member.presentation.dto.request;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode
public class MemberNotificationAllReadRequest {
	@NotNull(message = "필수 정보입니다")
	@Size(min = 1, message = "읽을 알림의 개수는 1개 이상이어야 합니다")
	@JsonProperty
	private final List<Long> notificationIds;

	@JsonCreator
	public MemberNotificationAllReadRequest(@JsonProperty("notificationIds") List<Long> notificationIds) {
		this.notificationIds = notificationIds;
	}

	public List<Long> notificationIds() {
		return new ArrayList<>(notificationIds);
	}

	@Override
	public String toString() {
		return String.format("MemberNotificationAllReadRequest(notificationIds=%s)", notificationIds);
	}
}
