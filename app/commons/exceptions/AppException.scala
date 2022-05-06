package commons.exceptions

sealed trait AppException extends RuntimeException {
  val statusCode: Int = 404
  val message: String
}

object AppException {
  case object MarginNotFoundException extends AppException {
    override val message: String = "Margin not found"
  }

  case object MarginRangeDoesNotMatchException extends AppException {
    override val message: String = "Margin range does not match"
  }

  case object MissingCurrentLoanMonthException extends AppException {
    override val message: String = "Missing current loan month"
  }

  case object CurrentLoanMonthCouldNotBeParsedException extends AppException {
    override val message: String = "Current loan month could not be parsed"
  }

  case object InvalidCurrentLoanMonthException extends AppException {
    override val message: String = "Invalid current loan month"
  }

  case object MissingLoanAmountPlnException extends AppException {
    override val message: String = "Missing loan amountPln"
  }

  case object LoanAmountPlnCouldNotBeParsedException extends AppException {
    override val message: String = "Loan amountPln could not be parsed"
  }

  case object InvalidLoanAmountPlnException extends AppException {
    override val message: String = "Invalid loan amountPln"
  }

  case object MissingOwnPaymentInPercentageException extends AppException {
    override val message: String = "Missing onw payment in percentage"
  }

  case object OwnPaymentInPercentageCouldNotBeParsedException extends AppException {
    override val message: String = "Onw payment in percentage could not be parsed"
  }

  case object InvalidOwnPaymentInPercentageException extends AppException {
    override val message: String = "Invalid own payment in percentage"
  }

  case object MissingLowerValueOfRangeException extends AppException {
    override val message: String = "Missing lower value of range"
  }

  case object MissingUpperValueOfRangeException extends AppException {
    override val message: String = "Missing upper value of range"
  }

  case object IncorrectLowerValueOfRangeException extends AppException {
    override val message: String = "Incorrect lower value of range"
  }

  case object IncorrectUpperValueOfRangeException extends AppException {
    override val message: String = "Incorrect upper value of range"
  }

  case object IncorrectValuesOfRangeException extends AppException {
    override val message: String = "Incorrect values of range exception, upper should be greater than lower"
  }

  case object SplitPointsContainsNegativeValueException extends AppException {
    override val message: String = "Split points contain negative value"
  }

  case object MissingSplitPointsListException extends AppException {
    override val message: String = "Missing split points list"
  }

  case object MissingRangeTypeException extends AppException {
    override val message: String = "Missing range type"
  }

  case object SuppliedDivisionsEmptyException extends AppException {
    override val message: String = "Supplied divisions empty"
  }

  case object MissingEnumerationValueException extends AppException {
    override val message: String = "Missing enumeration value"
  }

  case object ConfigNotFoundException extends AppException {
    override val message: String = "Config not found"
  }

  case object MissingOwnPaymentException extends AppException {
    override val message: String = "Missing own payment"
  }

  case object LoanAmountContainsNegativeValueException extends AppException {
    override val message: String = "Loan amount contains negative value"
  }

  case object MarginContainsNegativeValueException extends AppException {
    override val message: String = "Margin contains negative value"
  }

  case object IncorrectCreatedMarginCountException extends AppException {
    override val message: String = "Incorrect created margin count"
  }
}
