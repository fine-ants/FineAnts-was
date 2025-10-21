package co.fineants.api.domain.watchlist.domain.dto.request;

import java.util.List;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DeleteWatchListsRequests {
	@NotNull(message = "필수 정보입니다")
	@Size(min = 1, message = "최소 1개의 관심종목 식별자가 필요합니다")
	private List<Long> watchlistIds;
}
