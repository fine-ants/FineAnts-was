package co.fineants.api.domain.stock.converter;

import co.fineants.stock.domain.Market;
import jakarta.persistence.AttributeConverter;

public class MarketConverter implements AttributeConverter<Market, String> {

	@Override
	public String convertToDatabaseColumn(Market market) {
		return market.name().replace("_", " ");
	}

	@Override
	public Market convertToEntityAttribute(String dbData) {
		return Market.ofMarket(dbData.replace(" ", "_"));
	}
}
