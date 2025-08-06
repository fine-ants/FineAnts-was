package co.fineants.api.domain.holding.domain.factory;

import static org.assertj.core.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.mockito.Mockito;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import co.fineants.api.domain.holding.domain.dto.response.PortfolioDetailRealTimeItem;
import co.fineants.api.domain.holding.domain.dto.response.PortfolioHoldingRealTimeItem;
import co.fineants.api.domain.holding.domain.message.PortfolioReturnsStreamMessage;
import co.fineants.api.domain.holding.domain.message.StreamMessage;

class PortfolioSseEventBuilderFactoryTest {

	@DisplayName("SseEventBuilder 인스턴스를 생성한다")
	@Test
	void shouldCreatedSseEventBuilder() {
		// given
		UuidGenerator uuidGenerator = Mockito.mock(UuidGenerator.class);
		String uuid = "09b0798d-46cf-4c97-aae5-3a6f0f687aed";
		BDDMockito.given(uuidGenerator.generate())
			.willReturn(uuid);
		long reconnectTimeMillis = 3000L;
		SseEventBuilderFactory factory = new PortfolioSseEventBuilderFactory(reconnectTimeMillis, uuidGenerator);
		PortfolioDetailRealTimeItem details = Mockito.mock(PortfolioDetailRealTimeItem.class);
		List<PortfolioHoldingRealTimeItem> portfolioHoldings = List.of(
			Mockito.mock(PortfolioHoldingRealTimeItem.class),
			Mockito.mock(PortfolioHoldingRealTimeItem.class)
		);
		StreamMessage message = new PortfolioReturnsStreamMessage(details, portfolioHoldings);
		// when
		SseEmitter.SseEventBuilder builder = factory.create(message);
		// then
		assertThat(builder).isNotNull();
	}
}
