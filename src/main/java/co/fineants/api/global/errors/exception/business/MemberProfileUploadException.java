package co.fineants.api.global.errors.exception.business;

import org.springframework.http.HttpStatus;
import org.springframework.web.multipart.MultipartFile;

import co.fineants.api.global.errors.errorcode.ErrorCode;

public class MemberProfileUploadException extends BusinessException {
	private final MultipartFile file;

	public MemberProfileUploadException(MultipartFile file, Exception e) {
		super(file.toString(), ErrorCode.MEMBER_PROFILE_NOT_CHANGE_BAD_REQUEST, e);
		this.file = file;
	}

	@Override
	public HttpStatus getHttpStatus() {
		return HttpStatus.BAD_REQUEST;
	}

	@Override
	public String getExceptionValue() {
		return file.toString();
	}
}
