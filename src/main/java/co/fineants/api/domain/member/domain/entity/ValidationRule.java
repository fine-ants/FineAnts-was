package co.fineants.api.domain.member.domain.entity;

public interface ValidationRule {
	void validate(String text);

	void validate(Member member);
}
