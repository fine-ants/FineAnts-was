package co.fineants.api.domain.holding.domain.factory;

public class DefaultUuidGenerator implements UuidGenerator {
	@Override
	public String generate() {
		return UuidGenerator.super.generate();
	}
}
