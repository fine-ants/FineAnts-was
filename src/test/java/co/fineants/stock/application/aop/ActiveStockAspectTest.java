package co.fineants.stock.application.aop;

import java.lang.reflect.Method;
import java.time.Duration;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.assertj.core.api.Assertions;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.BDDMockito;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;

import co.fineants.AbstractContainerBaseTest;
import co.fineants.api.domain.holding.repository.PortfolioHoldingRepository;
import co.fineants.api.domain.portfolio.domain.entity.Portfolio;
import co.fineants.api.domain.portfolio.repository.PortfolioRepository;
import co.fineants.member.domain.Member;
import co.fineants.member.domain.MemberRepository;
import co.fineants.stock.annotation.ActiveStockMarker;
import co.fineants.stock.annotation.ResourceType;
import co.fineants.stock.domain.ActiveStockRepository;
import co.fineants.stock.domain.Stock;
import co.fineants.stock.domain.StockRepository;

class ActiveStockAspectTest extends AbstractContainerBaseTest {

	@Autowired
	private ActiveStockAspect aspect;

	@Mock
	private JoinPoint joinPoint;

	@Mock
	private MethodSignature methodSignature;

	@Autowired
	private ActiveStockRepository activeStockRepository;

	@Autowired
	private MemberRepository memberRepository;

	@Autowired
	private StockRepository stockRepository;

	@Autowired
	private PortfolioRepository portfolioRepository;

	@Autowired
	private PortfolioHoldingRepository portfolioHoldingRepository;
	private Portfolio portfolio;

	@BeforeEach
	void setUp() throws NoSuchMethodException {
		Stock stock = stockRepository.save(createSamsungStock());
		Member member = memberRepository.save(createMember());
		portfolio = portfolioRepository.save(createPortfolio(member));
		portfolioHoldingRepository.save(createPortfolioHolding(portfolio, stock));

		MockitoAnnotations.openMocks(this);
		// JoinPoint 설정 : 메서드 인자와 파라미터 설정
		Method method = TestTarget.class.getMethod("sampleMethod", Long.class, Long.class);
		BDDMockito.given(joinPoint.getSignature())
			.willReturn(methodSignature);
		BDDMockito.given(methodSignature.getMethod())
			.willReturn(method);
		BDDMockito.given(joinPoint.getArgs())
			.willReturn(new Object[] {member.getId(), portfolio.getId()});
	}

	@DisplayName("객체 생성")
	@Test
	void canCreated() {
		Assertions.assertThat(aspect).isNotNull();
	}

	@DisplayName("활성 종목 등록 - 회원이 가진 포트폴리오 종목들을 활성 종목으로 등록한다.")
	@Test
	void markBeforeController_memberPortfolioStocks_registerActiveStocks() {
		// given
		ActiveStockMarker marker = createMarker("#memberId", ResourceType.MEMBER);

		// when
		aspect.markBeforeController(joinPoint, marker);

		// then
		Awaitility.await()
			.atMost(Duration.ofSeconds(5))
			.untilAsserted(() -> Assertions.assertThat(activeStockRepository.size()).isEqualTo(1L));
	}

	@DisplayName("활성 종목 등록 - 특정 포트폴리오에 등록된 종목들을 활성 종목으로 등록한다")
	@Test
	void markBeforeController_whenResourceIdIsPortfolioId_registerActiveStocks() {
		// given
		ActiveStockMarker marker = createMarker("#portfolioId", ResourceType.PORTFOLIO);

		// when
		aspect.markBeforeController(joinPoint, marker);

		// then
		Awaitility.await()
			.atMost(Duration.ofSeconds(5))
			.untilAsserted(() -> Assertions.assertThat(activeStockRepository.size()).isEqualTo(1L));
	}

	@DisplayName("활성 종목 등록 실패 - 잘못된 ResourceId를 전달하면 예외가 발생한다.")
	@ParameterizedTest
	@MethodSource(value = {"co.fineants.TestDataProvider#invalidResourceIds"})
	void markBeforeController_invalidResourceId_throwsException(String invalidResourceId) {
		// given
		ActiveStockMarker marker = createMarker(invalidResourceId, ResourceType.MEMBER);

		// when & then
		Assertions.assertThatThrownBy(() -> aspect.markBeforeController(joinPoint, marker))
			.isInstanceOf(IllegalStateException.class)
			.hasMessageContaining("Failed to process ActiveStockMarker");
	}

	private ActiveStockMarker createMarker(String resourceId, ResourceType type) {
		return new ActiveStockMarker() {
			@Override
			public String resourceId() {
				return resourceId;
			}

			@Override
			public ResourceType type() {
				return type;
			}

			@Override
			public Class<? extends java.lang.annotation.Annotation> annotationType() {
				return ActiveStockMarker.class;
			}
		};
	}

	private static class TestTarget {
		public void sampleMethod(Long memberId, Long portfolioId) {
		}
	}
}
