package co.fineants.api.infra.mail;

import co.fineants.api.global.errors.exception.email.EmailSendException;

public interface EmailService {
	/**
	 * 이메일을 전송한다
	 *
	 * @param to 수신자
	 * @param subject 제목
	 * @param body 내용
	 * @throws EmailSendException 이메일을 전송하지 못하면 예외가 발생함
	 */
	void sendEmail(String to, String subject, String body) throws EmailSendException;
}
