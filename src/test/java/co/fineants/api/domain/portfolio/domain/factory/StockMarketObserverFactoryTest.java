package co.fineants.api.domain.portfolio.domain.factory;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import io.reactivex.rxjava3.core.Observer;

class StockMarketObserverFactoryTest {

	private ObserverFactory<String> factory;

	@BeforeEach
	void setUp() {
		long reconnectTimeMillis = 3000L;
		factory = new StockMarketObserverFactory(reconnectTimeMillis);
	}

	@DisplayName("SseEmitter를 인자로 받아 Observer를 생성한다.")
	@Test
	void givenSseEmitter_whenCreateEmitter_thenReturnObserver() {
		// given
		SseEmitter emitter = new SseEmitter(1000L * 40L);
		// when
		Observer<String> observer = factory.create(emitter);
		// then
		Assertions.assertThat(observer).isNotNull();
	}
}
