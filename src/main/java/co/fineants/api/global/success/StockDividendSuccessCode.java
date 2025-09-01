package co.fineants.api.global.success;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@Getter
@RequiredArgsConstructor
@ToString
public enum StockDividendSuccessCode implements SuccessCode {
	OK_INIT_DIVIDENDS(HttpStatus.OK, "배당 일정이 초기화되었습니다"),
	OK_WRITE_DIVIDENDS_CSV(HttpStatus.OK, "배당금 데이터 작성에 성공하였습니다"),
	OK_REFRESH_DIVIDENDS(HttpStatus.OK, "배당 일정 최신화 완료");

	private final HttpStatus httpStatus;
	private final String message;
}
