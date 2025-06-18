package co.fineants.api.domain.validator;

public interface Validator<T> {
	void validate(T target);
}
