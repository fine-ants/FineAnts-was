package co.fineants.api.infra.mail;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class EmailTemplate {
	private final String subject;
	private final String body;
}
