package co.fineants.api.domain.portfolio.domain.calculator;

import java.util.List;

import co.fineants.api.domain.common.money.Bank;
import co.fineants.api.domain.common.money.Expression;
import co.fineants.api.domain.common.money.Money;
import co.fineants.api.domain.common.money.RateDivision;
import co.fineants.api.domain.gainhistory.domain.entity.PortfolioGainHistory;
import co.fineants.api.domain.holding.domain.entity.PortfolioHolding;
import co.fineants.api.domain.portfolio.domain.entity.Portfolio;
import co.fineants.api.global.common.time.LocalDateTimeService;

public class PortfolioCalculator {

	public Expression calTotalGainBy(Portfolio portfolio) {
		return portfolio.calTotalGain(this);
	}

	/**
	 * 포트폴리오 총 손익(TotalGain) 계산
	 * <p>
	 * TotalGain = 각 종목(holdings)의 총 손익(TotalGain) 합계
	 * </p>
	 *
	 * @param holdings 포트폴리오 종목 리스트
	 * @return 포트폴리오 총 손익 계산 합계
	 */
	public Expression calTotalGain(List<PortfolioHolding> holdings) {
		return holdings.stream()
			.map(PortfolioHolding::calculateTotalGain)
			.reduce(Money.zero(), Expression::plus);
	}

	public Expression calTotalGainRateBy(Portfolio portfolio) {
		return portfolio.calTotalGainRate(this);
	}

	/**
	 * 포트폴리오 총 손익율(TotalGainRate) 계산.
	 * <p>
	 * TotalGainRate = (TotalGain / TotalInvestment)
	 * </p>
	 * @param holdings 포트폴리오 종목 리스트
	 * @return 포트폴리오 총 손익율(TotalGainRate)
	 */
	public Expression calTotalGainRate(List<PortfolioHolding> holdings) {
		Expression totalGain = calTotalGain(holdings);
		Expression totalInvestment = calTotalInvestment(holdings);
		return totalGain.divide(totalInvestment);
	}

	public Expression calTotalInvestmentBy(Portfolio portfolio) {
		return portfolio.calTotalInvestment(this);
	}

	/**
	 * 포트폴리오 총 투자 금액 계산
	 * <p>
	 * TotalInvestment = 각 종목들(holdings)의 총 투자 금액 합계
	 * </p>
	 * @return 포트폴리오 총 투자 금액 합계
	 */
	public Expression calTotalInvestment(List<PortfolioHolding> holdings) {
		return holdings.stream()
			.map(PortfolioHolding::calculateTotalInvestmentAmount)
			.reduce(Money.wonZero(), Expression::plus);
	}

	/**
	 * 포트폴리오 잔고 계산
	 * <p>
	 * Balance = Budget - TotalInvestment
	 * </p>
	 * @param portfolio 포트폴리오
	 * @return 포트폴리오의 잔고
	 */
	public Expression calBalanceBy(Portfolio portfolio) {
		Expression totalInvestment = calTotalInvestmentBy(portfolio);
		return portfolio.calBalance(totalInvestment);
	}

	public Expression calTotalCurrentValuationBy(Portfolio portfolio) {
		return portfolio.calTotalCurrentValuation(this);
	}

	/**
	 * 포트폴리오 평가 금액 계산
	 * <p>
	 * TotalCurrentValuation = 각 종목들(holdings)의 평가 금액 합계
	 * </p>
	 * @param holdings 포트폴리오에 등록된 종목 리스트
	 * @return 포트폴리오 평가 금액
	 */
	public Expression calTotalCurrentValuation(List<PortfolioHolding> holdings) {
		return holdings.stream()
			.map(PortfolioHolding::calculateCurrentValuation)
			.reduce(Money.zero(), Expression::plus);
	}

	public Expression calTotalAssetBy(Portfolio portfolio) {
		return portfolio.calTotalAsset(this);
	}

	public Expression calTotalAsset(Expression balance, Expression totalCurrentValuation) {
		return balance.plus(totalCurrentValuation);
	}

	/**
	 * 포트폴리오 당일 손익을 계산하여 반환.
	 *
	 * <p>
	 * dailyGain = totalCurrentValuation - previousCurrentValuation
	 * 이전일의 포트포릴오 내역의 각 종목들의 평가 금액이 없는 경우 총 투자금액으로 뺀다
	 * </p>
	 * @param history 이전 포트폴리오 내역
	 * @return 포트폴리오 당일 손익
	 */
	public Expression calDailyGain(PortfolioGainHistory history, Portfolio portfolio) {
		Expression previousCurrentValuation = history.getCurrentValuation();
		Money previousCurrentValuationMoney = Bank.getInstance().toWon(previousCurrentValuation);
		Expression totalCurrentValuation = calTotalCurrentValuationBy(portfolio);
		if (previousCurrentValuationMoney.isZero()) {
			Expression totalInvestment = calTotalInvestmentBy(portfolio);
			return totalCurrentValuation.minus(totalInvestment);
		}
		return totalCurrentValuation.minus(previousCurrentValuation);
	}

	/**
	 * 포트폴리오의 당일 손익율을 계산하여 반환.
	 *
	 * <p>
	 * 이전 포트폴리오 내역이 없는 경우
	 * dailyGainRate = (totalCurrentValuation - totalInvestment) / totalInvestment
	 * 이전 포트폴리오 내역이 있는 경우
	 * dailyGainRate = (totalCurrentValuation - previousCurrentValuation) / previousCurrentValuation
	 * </p>
	 *
	 * @param history 이전 포트폴리오 내역
	 * @param portfolio 포트폴리오 객체
	 * @return 포트폴리오의 당일 손익율
	 */
	public Expression calDailyGainRateBy(PortfolioGainHistory history, Portfolio portfolio) {
		Money prevCurrentValuation = history.getCurrentValuation();
		Expression totalCurrentValuation = calTotalCurrentValuationBy(portfolio);
		if (prevCurrentValuation.isZero()) {
			Expression totalInvestment = calTotalInvestmentBy(portfolio);
			return totalCurrentValuation.minus(totalInvestment)
				.divide(totalInvestment);
		}
		return totalCurrentValuation.minus(prevCurrentValuation)
			.divide(prevCurrentValuation);
	}

	public Expression calCurrentMonthDividendBy(Portfolio portfolio) {
		return portfolio.calCurrentMonthDividend(this);
	}

	/**
	 * 포트폴리오의 당월 예상 배당금 계산후 반환한다.
	 * <p>
	 * 당월 예상 배당금 = 각 종목들(holdings)의 해당월의 배당금 합계
	 * </p>
	 * @param holdings 포트폴리오 종목 리스트
	 * @return 포트폴리오의 당월 예상 배당금 합계
	 */
	public Expression calCurrentMonthDividend(List<PortfolioHolding> holdings) {
		return holdings.stream()
			.map(PortfolioHolding::calculateCurrentMonthDividend)
			.reduce(Money.zero(), Expression::plus);
	}

	public Expression calAnnualDividendBy(LocalDateTimeService dateTimeService, Portfolio portfolio) {
		return portfolio.calAnnualDividend(dateTimeService, this);
	}

	/**
	 * 포트폴리오의 총 연간 배당금 계산 후 반환한다.
	 *
	 * <p>
	 * AnnualDividend = 각 종목들(holdings)의 연간 배당금 합계
	 * </p>
	 * @param dateTimeService 시간 서비스
	 * @param holdings 포트폴리오의 종목 리스트
	 * @return 포트폴리오의 총 연간 배당금
	 */
	public Expression calAnnualDividend(LocalDateTimeService dateTimeService, List<PortfolioHolding> holdings) {
		return holdings.stream()
			.map(portfolioHolding -> portfolioHolding.createMonthlyDividendMap(
				dateTimeService.getLocalDateWithNow()))
			.map(map -> map.values().stream()
				.reduce(Money.zero(), Expression::plus))
			.reduce(Money.zero(), Expression::plus);
	}

	/**
	 * 포트폴리오의 총 연간 배당율을 계산 후 반환한다.
	 *
	 * <p>
	 * AnnualDividendYield = (TotalAnnualDividend / TotalCurrentValuation)
	 * </p>
	 * @param localDateTimeService 시간 서비스
	 * @param portfolio 포트폴리오 객체
	 * @return 포트폴리오의 총 연간 배당율
	 */
	public Expression calAnnualDividendYieldBy(LocalDateTimeService localDateTimeService, Portfolio portfolio) {
		Expression totalAnnualDividend = calAnnualDividendBy(localDateTimeService, portfolio);
		Expression totalCurrentValuation = calTotalCurrentValuationBy(portfolio);
		return totalAnnualDividend.divide(totalCurrentValuation);
	}

	public Expression calAnnualInvestmentDividendYieldBy(LocalDateTimeService localDateTimeService,
		Portfolio portfolio) {
		return portfolio.calAnnualInvestmentDividendYield(localDateTimeService, this);
	}

	/**
	 * 포트폴리오의 투자 대비 연간 배당율 계산 후 반환한다.
	 * <p>
	 * AnnualInvestmentDividendYield = AnnualDividend / TotalInvestment
	 * </p>
	 * @param annualDividend 포트폴리오 연간 배당금
	 * @param totalInvestment 포트폴리오 총 투자 금액
	 * @return 포트폴리오의 투자 대비 연간 배당율
	 */
	public Expression calAnnualInvestmentDividendYield(Expression annualDividend, Expression totalInvestment) {
		return annualDividend.divide(totalInvestment);
	}

	/**
	 * 포트폴리오의 최대손실율 계산 후 반환.
	 * <p>
	 * MaximumLossRate = ((Budget - MaximumLoss) / Budget)
	 * </p>
	 * @param portfolio 포트폴리오 객체
	 * @return 포트폴리오의 최대손실율
	 */
	public Expression calMaximumLossRateBy(Portfolio portfolio) {
		return portfolio.calculateMaximumLossRate();
	}

	/**
	 * 포트폴리오의 목표수익금액율 계산 후 반환.
	 * <p>
	 * TargetGainRate = ((TargetGain - Budget) / Budget)
	 * </p>
	 * @param portfolio 포트폴리오 객체
	 * @return 포트폴리오의 목표수익금액율
	 */
	public Expression calTargetGainRateBy(Portfolio portfolio) {
		return portfolio.calculateTargetReturnRate();
	}

	/**
	 * 포트폴리오의 현금 비중을 계산 후 반환.
	 * <p>
	 * CashWeight = Balance / TotalAsset
	 * </p>
	 * @param portfolio 포트폴리오 객체
	 * @return 포트폴리오의 현금 비중
	 */
	public RateDivision calCashWeightBy(Portfolio portfolio) {
		Expression balance = calBalanceBy(portfolio);
		Expression totalAsset = calTotalAssetBy(portfolio);
		return balance.divide(totalAsset);
	}
}