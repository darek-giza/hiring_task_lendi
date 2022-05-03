package commons.exceptions

sealed trait AppException extends RuntimeException {
  val statusCode: Int
  val message: String
}

object AppException {
  case object MarginNotFoundException extends AppException {
    override val statusCode: Int = 404
    override val message: String = "Margin not found exception"
  }

  case object MarginRangeDoesNotMatchException extends AppException {
    override val statusCode: Int = 404
    override val message: String = "Margin range does not match exception"
  }

  case object MissingCurrentLoanMonthException extends AppException {
    override val statusCode: Int = 404
    override val message: String = "Missing current loan month exception"
  }

  case object CurrentLoanMonthCouldNotBeParsedException extends AppException {
    override val statusCode: Int = 404
    override val message: String = "Current loan month could not be parsed exception"
  }

  case object InvalidCurrentLoanMonthException extends AppException {
    override val statusCode: Int = 404
    override val message: String = "Invalid current loan month exception"
  }

  case object MissingLoanAmountPlnException extends AppException {
    override val statusCode: Int = 404
    override val message: String = "Missing loan amountPln exception"
  }

  case object LoanAmountPlnCouldNotBeParsedException extends AppException {
    override val statusCode: Int = 404
    override val message: String = "Loan amountPln could not be parsed exception"
  }

  case object InvalidLoanAmountPlnException extends AppException {
    override val statusCode: Int = 404
    override val message: String = "Invalid loan amountPln exception"
  }

  case object MissingOwnPaymentInPercentageException extends AppException {
    override val statusCode: Int = 404
    override val message: String = "Missing onw payment in percentage exception"
  }

  case object OwnPaymentInPercentageCouldNotBeParsedException extends AppException {
    override val statusCode: Int = 404
    override val message: String = "Onw payment in percentage could not be parsed exception"
  }

  case object InvalidOwnPaymentInPercentageException extends AppException {
    override val statusCode: Int = 404
    override val message: String = "Invalid own payment in percentage exception"
  }
}
