package co.fineants.api.infra.s3.service.imple;

import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import co.fineants.AbstractContainerBaseTest;
import co.fineants.api.infra.s3.dto.StockDividendDto;
import co.fineants.api.infra.s3.service.FetchDividendService;
import co.fineants.config.GoogleCloudStorageBucketInitializer;
import co.fineants.config.GoogleCloudStorageTestConfig;

@ActiveProfiles(value = {"test", "gcp"}, inheritProfiles = false)
@ContextConfiguration(classes = {GoogleCloudStorageTestConfig.class, GoogleCloudStorageBucketInitializer.class})
class GoogleCloudStorageFetchDividendServiceTest extends AbstractContainerBaseTest {

	@Test
	void canCreated() {
		FetchDividendService service = new GoogleCloudStorageFetchDividendService();

		Assertions.assertThat(service).isNotNull();
	}

	@Test
	void fetchDividend() {
		FetchDividendService service = new GoogleCloudStorageFetchDividendService();

		List<StockDividendDto> dtos = service.fetchDividend();

		Assertions.assertThat(dtos).isEmpty();
	}
}
