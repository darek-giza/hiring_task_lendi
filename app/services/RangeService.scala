package services

import models.Margin

trait RangeService {
  def matchLoanAmountRange(margin: Margin, amount: BigDecimal): Boolean
  def matchOwnPaymentRange(margin: Margin, payment: BigDecimal): Boolean
}

class RangeServiceImpl extends RangeService {
  override def matchOwnPaymentRange(m: Margin, payment: BigDecimal): Boolean =
    payment >= m.ownPaymentRangeInPercentage.min && payment < m.ownPaymentRangeInPercentage.max

  override def matchLoanAmountRange(m: Margin, amount: BigDecimal): Boolean =
    amount >= m.loanAmountRangePLN.min && amount < m.loanAmountRangePLN.max
}
