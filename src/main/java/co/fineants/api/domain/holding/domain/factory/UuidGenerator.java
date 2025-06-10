package co.fineants.api.domain.holding.domain.factory;

import java.util.UUID;

public interface UuidGenerator {
	default String generate() {
		return UUID.randomUUID().toString();
	}
}
