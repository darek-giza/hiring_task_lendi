package commons.validator

import commons.exceptions.AppException.{
  CurrentLoanMonthCouldNotBeParsedException,
  InvalidCurrentLoanMonthException,
  InvalidLoanAmountPlnException,
  InvalidOwnPaymentInPercentageException,
  LoanAmountPlnCouldNotBeParsedException,
  MissingCurrentLoanMonthException,
  MissingLoanAmountPlnException,
  MissingOwnPaymentInPercentageException,
  OwnPaymentInPercentageCouldNotBeParsedException
}
import models.MarginFormDto
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerTest

class ParamsValidatorSpec extends PlaySpec with GuiceOneAppPerTest {

  "ParamsValidator" should {
    "returns MissingCurrentLoanMonthException" in {
      val valid = ParamsValidator.validateParams(None, Some("1000"), Some("50"))

      valid mustBe Left(MissingCurrentLoanMonthException)
    }

    "returns CurrentLoanMonthCouldNotBeParsedException" in {
      val valid = ParamsValidator.validateParams(Some(""), Some("1000"), Some("50"))

      valid mustBe Left(CurrentLoanMonthCouldNotBeParsedException)
    }

    "returns InvalidCurrentLoanMonthException" in {
      val valid = ParamsValidator.validateParams(Some("-1"), Some("1000"), Some("50"))

      valid mustBe Left(InvalidCurrentLoanMonthException)
    }

    "returns MissingLoanAmountPlnException" in {
      val valid = ParamsValidator.validateParams(Some("1"), None, Some("50"))

      valid mustBe Left(MissingLoanAmountPlnException)
    }

    "returns LoanAmountPlnCouldNotBeParsedException" in {
      val valid = ParamsValidator.validateParams(Some("1"), Some(""), Some("50"))

      valid mustBe Left(LoanAmountPlnCouldNotBeParsedException)
    }

    "returns InvalidLoanAmountPlnException" in {
      val valid = ParamsValidator.validateParams(Some("1"), Some("-1"), Some("50"))

      valid mustBe Left(InvalidLoanAmountPlnException)
    }

    "returns MissingOwnPaymentInPercentageException" in {
      val valid = ParamsValidator.validateParams(Some("1"), Some("1000"), None)

      valid mustBe Left(MissingOwnPaymentInPercentageException)
    }

    "returns OwnPaymentInPercentageCouldNotBeParsedException" in {
      val valid = ParamsValidator.validateParams(Some("1"), Some("1000"), Some(""))

      valid mustBe Left(OwnPaymentInPercentageCouldNotBeParsedException)
    }

    "returns InvalidOwnPaymentInPercentageException" in {
      val valid = ParamsValidator.validateParams(Some("1"), Some("1000"), Some("0"))

      valid mustBe Left(InvalidOwnPaymentInPercentageException)
    }

    "returns valid margin form dto" in {
      val marginFormDto = MarginFormDto(12, BigDecimal("1000.00"), BigDecimal("50.00"))

      val valid = ParamsValidator.validateParams(Some("12"), Some("1000"), Some("50"))

      valid mustBe Right(marginFormDto)
    }
  }
}
