package commons.validator

object Validator {

  val MAX_DURATION_OF_THE_LOAN_IN_MONTH = 360
  val MAX_LOAN_AMOUNT_IN_PLN = 5000000

  def isValidCurrentLoanMonth(i: Int): Boolean =
    i >= 1 && i <= MAX_DURATION_OF_THE_LOAN_IN_MONTH

  def isValidLoanAmountPln(i: BigDecimal): Boolean =
    i > 0 && i <= MAX_LOAN_AMOUNT_IN_PLN

  def isValidOwnPaymentInPercentage(i: BigDecimal): Boolean =
    i > 0 && i < 100
}
