package co.fineants.api.domain.watchlist.domain.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor
public class ChangeWatchListNameRequest {
	@NotBlank(message = "이름은 필수 입력 항목입니다")
	private String name;
}
