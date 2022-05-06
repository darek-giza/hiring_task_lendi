package commons.validator

import commons.exceptions.AppException.{
  IncorrectLowerValueOfRangeException,
  IncorrectUpperValueOfRangeException,
  IncorrectValuesOfRangeException,
  InvalidOwnPaymentInPercentageException,
  LoanAmountContainsNegativeValueException,
  MarginContainsNegativeValueException,
  SplitPointsContainsNegativeValueException
}
import models.{ ConfigFormDto, ItemDto, MarginsFormDto, RangeType }
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerTest

class FormValidatorSpec extends PlaySpec with GuiceOneAppPerTest {

  "FormValidator" should {
    "returns IncorrectLowerValueOfRangeException" in {
      val formOpt = Some(ConfigFormDto(-10, 100, Set.empty, RangeType.PAYMENT))
      val valid = FormValidator.validateConfigForm(formOpt)

      valid mustBe Left(IncorrectLowerValueOfRangeException)
    }

    "returns IncorrectUpperValueOfRangeException" in {
      val formOpt = Some(ConfigFormDto(10, -100, Set.empty, RangeType.PAYMENT))
      val valid = FormValidator.validateConfigForm(formOpt)

      valid mustBe Left(IncorrectUpperValueOfRangeException)
    }

    "returns IncorrectValuesOfRangeException" in {
      val formOpt = Some(ConfigFormDto(100, 10, Set.empty, RangeType.PAYMENT))
      val valid = FormValidator.validateConfigForm(formOpt)

      valid mustBe Left(IncorrectValuesOfRangeException)
    }

    "returns SplitPointsContainsNegativeValueException" in {
      val formOpt = Some(ConfigFormDto(10, 100, Set(20, 30, -50), RangeType.PAYMENT))
      val valid = FormValidator.validateConfigForm(formOpt)

      valid mustBe Left(SplitPointsContainsNegativeValueException)
    }

    "returns InvalidOwnPaymentInPercentageException" in {
      val item1 = ItemDto(BigDecimal("20"), BigDecimal("25000"), BigDecimal("3.9"))
      val item2 = ItemDto(BigDecimal("-50"), BigDecimal("25000"), BigDecimal("3.7"))

      val valid = FormValidator.validateMarginsForm(Some(MarginsFormDto(List(item1, item2))))

      valid mustBe Left(InvalidOwnPaymentInPercentageException)
    }

    "returns LoanAmountContainsNegativeValueException" in {
      val item1 = ItemDto(BigDecimal("20"), BigDecimal("25000"), BigDecimal("3.9"))
      val item2 = ItemDto(BigDecimal("50"), BigDecimal("-25000"), BigDecimal("3.7"))

      val valid = FormValidator.validateMarginsForm(Some(MarginsFormDto(List(item1, item2))))

      valid mustBe Left(LoanAmountContainsNegativeValueException)
    }

    "returns MarginContainsNegativeValueException" in {
      val item1 = ItemDto(BigDecimal("20"), BigDecimal("25000"), BigDecimal("3.9"))
      val item2 = ItemDto(BigDecimal("50"), BigDecimal("25000"), BigDecimal("-3.7"))

      val valid = FormValidator.validateMarginsForm(Some(MarginsFormDto(List(item1, item2))))

      valid mustBe Left(MarginContainsNegativeValueException)
    }
  }
}
