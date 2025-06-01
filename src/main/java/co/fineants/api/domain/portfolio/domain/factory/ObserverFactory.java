package co.fineants.api.domain.portfolio.domain.factory;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import io.reactivex.rxjava3.core.Observer;

public interface ObserverFactory<R> {
	Observer<R> create(SseEmitter emitter);
}
