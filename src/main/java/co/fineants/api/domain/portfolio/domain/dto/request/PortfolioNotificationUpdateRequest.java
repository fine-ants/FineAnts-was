package co.fineants.api.domain.portfolio.domain.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@ToString
public class PortfolioNotificationUpdateRequest {
	@JsonProperty("isActive")
	@NotNull(message = "활성화/비활성 정보는 필수정보입니다.")
	private Boolean isActive;

	public static PortfolioNotificationUpdateRequest active() {
		return new PortfolioNotificationUpdateRequest(true);
	}
}
