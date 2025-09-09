package co.fineants.api.domain.holding.domain.dto.response;

import co.fineants.api.domain.common.money.Percentage;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@ToString
@EqualsAndHashCode
public class PortfolioSectorChartItem implements Comparable<PortfolioSectorChartItem> {
	private String sector;
	private Percentage sectorWeight;

	public static PortfolioSectorChartItem create(String sector, Percentage sectorWeight) {
		return new PortfolioSectorChartItem(sector, sectorWeight);
	}

	@Override
	public int compareTo(@NotNull PortfolioSectorChartItem item) {
		int result = item.getSectorWeight().compareTo(sectorWeight);
		if (result == 0) {
			return sector.compareTo(item.getSector());
		}
		return result;
	}
}
