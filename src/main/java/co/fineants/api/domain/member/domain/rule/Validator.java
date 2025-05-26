package co.fineants.api.domain.member.domain.rule;

public interface Validator<T> {
	void validate(T target);
}
