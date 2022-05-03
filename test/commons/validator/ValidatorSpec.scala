package commons.validator

import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerTest

class ValidatorSpec extends PlaySpec with GuiceOneAppPerTest {

  "Validator should" should {
    "isValidCurrentLoanMonth when is in range <1, 360>" in {
      Validator.isValidCurrentLoanMonth(-1) mustBe false
      Validator.isValidCurrentLoanMonth(0) mustBe false
      Validator.isValidCurrentLoanMonth(361) mustBe false

      Validator.isValidCurrentLoanMonth(1) mustBe true
      Validator.isValidCurrentLoanMonth(12) mustBe true
      Validator.isValidCurrentLoanMonth(13) mustBe true
      Validator.isValidCurrentLoanMonth(360) mustBe true
    }
  }

  "isValidLoanAmountPln when is in range (0, 5000000>" in {
    Validator.isValidLoanAmountPln(-1.00) mustBe false
    Validator.isValidLoanAmountPln(0.00) mustBe false
    Validator.isValidLoanAmountPln(5000000.01) mustBe false

    Validator.isValidLoanAmountPln(0.01) mustBe true
    Validator.isValidLoanAmountPln(10000.00) mustBe true
    Validator.isValidLoanAmountPln(5000000.00) mustBe true
  }

  "isValidOwnPaymentInPercentage when is in range (0, 100)" in {
    Validator.isValidOwnPaymentInPercentage(-1) mustBe false
    Validator.isValidOwnPaymentInPercentage(0) mustBe false
    Validator.isValidOwnPaymentInPercentage(100) mustBe false

    Validator.isValidOwnPaymentInPercentage(0.01) mustBe true
    Validator.isValidOwnPaymentInPercentage(10.00) mustBe true
    Validator.isValidOwnPaymentInPercentage(99.99) mustBe true
  }
}
