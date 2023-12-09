package codesquad.fineants.spring.api.portfolio.request;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class PortfoliosDeleteRequest {
	private List<Long> portfolioIds;
}
