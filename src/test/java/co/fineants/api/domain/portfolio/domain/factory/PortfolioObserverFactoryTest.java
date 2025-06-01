package co.fineants.api.domain.portfolio.domain.factory;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import co.fineants.api.domain.holding.domain.dto.response.PortfolioHoldingsRealTimeResponse;
import io.reactivex.rxjava3.core.Observer;

class PortfolioObserverFactoryTest {

	ObserverFactory<PortfolioHoldingsRealTimeResponse> factory;

	@BeforeEach
	void setUp() {
		long reconnectTimeMillis = 3000L;
		factory = new PortfolioObserverFactory(reconnectTimeMillis);
	}

	@DisplayName("SseEmitter를 인자로 받아 Observer를 생성한다.")
	@Test
	void givenSseEmitter_whenCreateObserver_thenReturnObserver() {
		// given
		SseEmitter emitter = new SseEmitter(1000L * 40L);
		// when
		Observer<PortfolioHoldingsRealTimeResponse> observer = factory.create(emitter);
		// then
		Assertions.assertThat(observer).isNotNull();
	}
}
