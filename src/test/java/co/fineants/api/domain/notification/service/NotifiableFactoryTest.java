package co.fineants.api.domain.notification.service;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import co.fineants.api.domain.kis.service.CurrentPriceService;
import co.fineants.api.domain.portfolio.repository.PortfolioRepository;
import co.fineants.api.domain.stock_target_price.repository.StockTargetPriceRepository;

@ExtendWith(MockitoExtension.class)
class NotifiableFactoryTest {

	@InjectMocks
	private NotifiableFactory factory;

	@Mock
	private PortfolioRepository portfolioRepository;

	@Mock
	private StockTargetPriceRepository stockTargetPriceRepository;

	@Mock
	private CurrentPriceService currentPriceService;

	@DisplayName("객체 생성")
	@Test
	void canCreated() {
		Assertions.assertThat(factory).isNotNull();
	}
}
