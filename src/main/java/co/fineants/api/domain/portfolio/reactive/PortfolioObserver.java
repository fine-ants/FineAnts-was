package co.fineants.api.domain.portfolio.reactive;

import java.io.IOException;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import co.fineants.api.domain.holding.domain.dto.response.PortfolioHoldingsRealTimeResponse;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class PortfolioObserver implements Observer<PortfolioHoldingsRealTimeResponse> {

	public static final String EVENT_NAME = "portfolioDetails";
	private final SseEmitter emitter;
	private final long reconnectTimeMillis;

	@Override
	public void onSubscribe(@NonNull Disposable d) {
		log.info("subscribe portfolio emitter {}", emitter.toString());
	}

	@Override
	public void onNext(@NonNull PortfolioHoldingsRealTimeResponse data) {
		String id = String.valueOf(System.currentTimeMillis());
		long reconnectTimeMillis = 3000;
		try {
			emitter.send(SseEmitter.event()
				.id(id)
				.data(data)
				.name(EVENT_NAME)
				.reconnectTime(reconnectTimeMillis));
		} catch (IOException e) {
			onError(e);
		}
	}

	@Override
	public void onError(@NonNull Throwable e) {
		log.error(e.getMessage());
		emitter.completeWithError(e);
	}

	@Override
	public void onComplete() {
		log.info("sseEmitter {} complete", emitter);
		emitter.complete();
	}
}
