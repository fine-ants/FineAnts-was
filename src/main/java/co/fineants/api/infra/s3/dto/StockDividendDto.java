package co.fineants.api.infra.s3.dto;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@AllArgsConstructor
@Getter
@EqualsAndHashCode
public class StockDividendDto {
	private Long id;
	private int dividend;
	private String recordDate;
	private String paymentDate;
	private String stockCode;

	public static StockDividendDto from(String[] parts) {
		return new StockDividendDto(
			Long.parseLong(parts[0]),
			Integer.parseInt(parts[1]),
			parts[2],
			parts[3],
			parts[4]
		);
	}
}
