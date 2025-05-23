package co.fineants.api.domain.kis.properties;

import co.fineants.api.domain.kis.properties.kiscodevalue.KisCodeValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum KisQueryParam {
	FID_COND_MRKT_DIV_CODE("FID_COND_MRKT_DIV_CODE", "조건 시장 분류 코드", KisCodeValue.class),
	FID_INPUT_ISCD("FID_INPUT_ISCD", "입력 종목코드"),
	FID_INPUT_DATE_1("FID_INPUT_DATE_1", "입력 날짜 (시작)"),
	FID_INPUT_DATE_2("FID_INPUT_DATE_2", "입력 날짜 (종료)"),
	FID_PERIOD_DIV_CODE("FID_PERIOD_DIV_CODE", "기간분류코드"),
	FID_ORG_ADJ_PRC("FID_ORG_ADJ_PRC", "수정주가 원주가 가격 여부"),
	HIGH_GB("HIGH_GB", "조회일자To"),
	CTS("CTS", "CTS"),
	GB1("GB1", "배당종류"),
	F_DT("F_DT", "조회일자From"),
	T_DT("T_DT", "조회일자To"),
	SHT_CD("SHT_CD", "종목코드"),
	PRDT_TYPE_CD("PRDT_TYPE_CD", "상품유형코드"),
	PDNO("PDNO", "상품번호"),
	BASS_DT("BASS_DT", "기준일자"),
	CTX_AREA_NK("CTX_AREA_NK", "연속조회키"),
	CTX_AREA_FK("CTX_AREA_FK", "연속조회검색조건");

	private final String paramName;
	private final String korName;
	private final Class<? extends KisCodeValue> allowedValues;

	KisQueryParam(String paramName, String korName) {
		this(paramName, korName, null);
	}
}
