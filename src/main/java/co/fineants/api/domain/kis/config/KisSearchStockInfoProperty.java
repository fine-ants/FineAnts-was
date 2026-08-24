package co.fineants.api.domain.kis.config;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum KisSearchStockInfoProperty {
	OUTPUT("output"),
	STD_PDNO("std_pdno"),             // 표준 상품 번호
	PDNO("pdno"),                     // 상품 번호
	PRDT_NAME("prdt_name"),           // 상품명
	PRDT_ENG_NAME("prdt_eng_name"),   // 상품 영문명
	MKET_ID_CD("mket_id_cd"),         // 시장 ID 코드
	IDX_BZTP_LCLS_CD_NAME("idx_bztp_lcls_cd_name"), // 업종 대분류
	IDX_BZTP_MCLS_CD_NAME("idx_bztp_mcls_cd_name"), // 업종 중분류
	IDX_BZTP_SCLS_CD_NAME("idx_bztp_scls_cd_name"), // 업종 소분류
	LSTG_ABOL_DT("lstg_abol_dt");     // 상장 폐지 일자

	private final String key;
}
