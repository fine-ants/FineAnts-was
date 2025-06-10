package co.fineants.api.domain.holding.domain.factory;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

class PortfolioSseEmitterFactoryTest {

	@DisplayName("SseEmitter 인스턴스를 생성한다")
	@Test
	void shouldReturnSseEmitter() {
		// given
		long timeout = 30000L;
		SseEmitterFactory factory = new PortfolioSseEmitterFactory(timeout);
		// when
		SseEmitter emitter = factory.create();
		// then
		Assertions.assertThat(emitter).isNotNull();
	}
}
