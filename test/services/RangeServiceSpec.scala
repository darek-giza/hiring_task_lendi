package services

import models.{ Margin, Range }
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerTest

class RangeServiceSpec extends PlaySpec with GuiceOneAppPerTest {

  sealed trait TestContext {
    val margin = Margin(Range(10, 20), Range(0, 40000), BigDecimal(3.49))
    val rangeService = new RangeServiceImpl
  }

  "RangeService" should {
    "in matchOwnPaymentRange" should {
      "returns false if value is not in range" in new TestContext {
        rangeService.matchOwnPaymentRange(margin, BigDecimal("9.00")) mustBe false
        rangeService.matchOwnPaymentRange(margin, BigDecimal("21.00")) mustBe false
        rangeService.matchOwnPaymentRange(margin, BigDecimal("25.00")) mustBe false
      }

      "returns true if value is in range" in new TestContext {
        rangeService.matchOwnPaymentRange(margin, BigDecimal("10.00")) mustBe true
        rangeService.matchOwnPaymentRange(margin, BigDecimal("15.00")) mustBe true
        rangeService.matchOwnPaymentRange(margin, BigDecimal("19.99")) mustBe true
      }
    }

    "in matchLoanAmountRange" should {
      "returns false if value is not in range" in new TestContext {
        rangeService.matchLoanAmountRange(margin, BigDecimal("-10.00")) mustBe false
        rangeService.matchLoanAmountRange(margin, BigDecimal("-1.00")) mustBe false
        rangeService.matchLoanAmountRange(margin, BigDecimal("-0.01")) mustBe false
      }

      "returns true if value is in range" in new TestContext {
        rangeService.matchLoanAmountRange(margin, BigDecimal("00.00")) mustBe true
        rangeService.matchLoanAmountRange(margin, BigDecimal("10000.00")) mustBe true
        rangeService.matchLoanAmountRange(margin, BigDecimal("39998.99")) mustBe true
      }
    }
  }
}
