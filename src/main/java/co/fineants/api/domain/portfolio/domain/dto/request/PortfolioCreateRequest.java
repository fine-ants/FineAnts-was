package co.fineants.api.domain.portfolio.domain.dto.request;

import co.fineants.api.domain.common.money.Money;
import co.fineants.api.domain.common.money.valiator.MoneyNumberWithZero;
import co.fineants.api.domain.member.domain.entity.Member;
import co.fineants.api.domain.portfolio.domain.entity.Portfolio;
import co.fineants.api.domain.portfolio.domain.entity.PortfolioDetail;
import co.fineants.api.domain.portfolio.domain.entity.PortfolioFinancial;
import co.fineants.api.domain.portfolio.properties.PortfolioProperties;
import co.fineants.api.global.errors.exception.domain.MaximumLossGreaterThanBudgetException;
import co.fineants.api.global.errors.exception.domain.MoneyNegativeException;
import co.fineants.api.global.errors.exception.domain.PortfolioNameInvalidException;
import co.fineants.api.global.errors.exception.domain.SecuritiesFirmNotContainException;
import co.fineants.api.global.errors.exception.domain.TargetGainLessThanBudgetException;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@ToString
public class PortfolioCreateRequest {
	@NotBlank(message = "포트폴리오 이름은 필수 정보입니다")
	@Pattern(regexp = PortfolioDetail.NAME_REGEXP, message = "유효하지 않은 포트폴리오 이름입니다.")
	private String name;

	@NotBlank(message = "증권사는 필수 정보입니다")
	private String securitiesFirm;

	@MoneyNumberWithZero
	private Money budget;

	@MoneyNumberWithZero
	private Money targetGain;

	@MoneyNumberWithZero
	private Money maximumLoss;

	public static PortfolioCreateRequest create(String name, String securitiesFirm, Money budget, Money targetGain,
		Money maximumLoss) {
		return new PortfolioCreateRequest(name, securitiesFirm, budget, targetGain, maximumLoss);
	}

	/**
	 * 포트폴리오 엔티티 객체를 생성하여 반환한다
	 *
	 * @param member 포트폴리오를 소유한 회원 객체
	 * @param properties 증권사 목록을 포함하는 프로퍼티 객체
	 * @return 포트폴리오 객체
	 * @throws PortfolioNameInvalidException 포트폴리오 이름이 유효하지 않으면 예외 발생
	 * @throws SecuritiesFirmNotContainException 증권사 목록에 없는 증권사 이름이면 예외 발생
	 * @throws MoneyNegativeException 예산, 목표 수익, 최대 손실 금액이 음수이면 예외 발생
	 * @throws TargetGainLessThanBudgetException 목표 수익 금액이 예산보다 크면 예외 발생
	 * @throws MaximumLossGreaterThanBudgetException 최대 손실 금액이 예산보다 크면 예외 발생
	 */
	public Portfolio toEntity(Member member, PortfolioProperties properties) throws
		PortfolioNameInvalidException,
		SecuritiesFirmNotContainException,
		MoneyNegativeException,
		TargetGainLessThanBudgetException,
		MaximumLossGreaterThanBudgetException {
		PortfolioDetail detail = PortfolioDetail.of(name, securitiesFirm, properties);
		PortfolioFinancial financial = PortfolioFinancial.of(budget, targetGain, maximumLoss);
		return Portfolio.allInActive(detail, financial, member);
	}
}
