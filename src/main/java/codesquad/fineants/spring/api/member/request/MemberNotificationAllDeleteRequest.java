package codesquad.fineants.spring.api.member.request;

import java.util.List;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MemberNotificationAllDeleteRequest {
	@NotNull(message = "필수 정보입니다")
	@Size(min = 1, message = "삭제할 알람의 개수는 1개 이상이어야 합니다")
	private List<Long> notificationIds;
}
