package co.fineants.api.domain.holding.domain.factory;

import org.springframework.stereotype.Component;

@Component
public class DefaultUuidGenerator implements UuidGenerator {
	@Override
	public String generate() {
		return UuidGenerator.super.generate();
	}
}
