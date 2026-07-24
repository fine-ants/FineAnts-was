package co.fineants.api.domain.kis.domain.dto.response;

import static co.fineants.api.domain.kis.config.KisSearchStockInfoProperty.*;

import java.util.HashMap;
import java.util.Map;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import co.fineants.api.domain.kis.config.KisSearchStockInfoProperty;
import co.fineants.api.global.util.ObjectMapperUtil;

class KisSearchStockInfoTest {

	@DisplayName("종목 정보를 역직렬화한다")
	@Test
	void givenJson_whenDeserialize_thenReturnInstance() {
		// given
		Map<String, Object> body = new HashMap<>();
		Map<String, Object> output = new HashMap<>();
		output.put(getText(STD_PDNO), "KR7000660001");
		output.put(getText(PDNO), "00000A000660");
		output.put(getText(PRDT_NAME), "에스케이하이닉스보통주");
		output.put(getText(PRDT_ENG_NAME), "SK hynix");
		output.put(getText(MKET_ID_CD), "STK");
		output.put(getText(IDX_BZTP_LCLS_CD_NAME), "시가총액규모대");
		output.put(getText(IDX_BZTP_MCLS_CD_NAME), "전기,전자");
		output.put(getText(IDX_BZTP_SCLS_CD_NAME), "전기,전자");
		output.put(getText(LSTG_ABOL_DT), "");
		body.put(getText(OUTPUT), output);

		String json = ObjectMapperUtil.serialize(body);
		// when
		KisSearchStockInfo actual = ObjectMapperUtil.deserialize(json, KisSearchStockInfo.class);
		// then
		KisSearchStockInfo expected = KisSearchStockInfo.listedStock("KR7000660001", "000660", "에스케이하이닉스보통주",
			"SK hynix", "STK", "시가총액규모대", "전기,전자", "전기,전자");
		Assertions.assertThat(actual).isEqualTo(expected);
	}

	private String getText(KisSearchStockInfoProperty property) {
		return property.getKey();
	}

}
