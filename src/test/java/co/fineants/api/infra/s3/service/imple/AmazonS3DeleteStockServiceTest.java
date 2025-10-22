package co.fineants.api.infra.s3.service.imple;

import java.util.List;
import java.util.NoSuchElementException;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import co.fineants.AbstractContainerBaseTest;
import co.fineants.TestDataFactory;
import co.fineants.stock.domain.Stock;
import co.fineants.api.infra.s3.service.DeleteStockService;
import co.fineants.api.infra.s3.service.FetchStockService;
import co.fineants.api.infra.s3.service.WriteStockService;

class AmazonS3DeleteStockServiceTest extends AbstractContainerBaseTest {

	@Autowired
	private DeleteStockService service;

	@Autowired
	private WriteStockService writeStockService;

	@Autowired
	private FetchStockService fetchStockService;

	@BeforeEach
	void setUp() {
		Stock samsungStock = TestDataFactory.createSamsungStock();
		Stock kakaoStock = TestDataFactory.createKakaoStock();

		writeStockService.writeStocks(List.of(samsungStock, kakaoStock));
	}

	@Test
	void canCreated() {
		Assertions.assertThat(service).isNotNull();
	}

	@Test
	void delete() {
		service.delete();

		Throwable throwable = Assertions.catchThrowable(() -> fetchStockService.fetchStocks());
		Assertions.assertThat(throwable)
			.isInstanceOf(NoSuchElementException.class);
	}

}
