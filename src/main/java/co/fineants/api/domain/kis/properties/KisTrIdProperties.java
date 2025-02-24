package co.fineants.api.domain.kis.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;

import lombok.Getter;

@Getter
@ConfigurationProperties(prefix = "oauth2.kis.tr-id")
public class KisTrIdProperties {

	private final String currentPrice;
	private final String closingPrice;
	private final String dividend;
	private final String ipo;
	private final String searchStockInfo;
	private final String websocketCurrentPrice;
	private final String holiday;

	@ConstructorBinding
	public KisTrIdProperties(String currentPrice, String closingPrice, String dividend, String ipo,
		String searchStockInfo, String websocketCurrentPrice, String holiday) {
		this.currentPrice = currentPrice;
		this.closingPrice = closingPrice;
		this.dividend = dividend;
		this.ipo = ipo;
		this.searchStockInfo = searchStockInfo;
		this.websocketCurrentPrice = websocketCurrentPrice;
		this.holiday = holiday;
	}
}
