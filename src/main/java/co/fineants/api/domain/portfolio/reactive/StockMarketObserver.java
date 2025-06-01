package co.fineants.api.domain.portfolio.reactive;

import java.io.IOException;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class StockMarketObserver implements Observer<String> {

	private static final String COMPLETE_NAME = "complete";
	private final SseEmitter emitter;

	public static StockMarketObserver create(SseEmitter emitter) {
		return new StockMarketObserver(emitter);
	}

	@Override
	public void onSubscribe(@NonNull Disposable d) {
		log.debug("StockMarketObserver onSubscribe");
	}

	@Override
	public void onNext(@NonNull String value) {
		String id = String.valueOf(System.currentTimeMillis());
		long reconnectTimeMillis = 3000;
		try {
			emitter.send(SseEmitter.event()
				.id(id)
				.data(value)
				.name(COMPLETE_NAME)
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
		emitter.complete();
	}
}
