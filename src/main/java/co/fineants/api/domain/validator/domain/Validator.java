package co.fineants.api.domain.validator.domain;

public interface Validator<T> {
	void validate(T target);
}
