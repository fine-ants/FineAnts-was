package co.fineants.member.presentation.dto.response;

import java.util.List;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor
@ToString
public class ListNotificationResponse {
	private List<NotificationDto> notifications;
}
