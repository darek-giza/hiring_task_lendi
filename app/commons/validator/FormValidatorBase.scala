package commons.validator

import commons.exceptions.AppException
import commons.exceptions.AppException.{
  IncorrectLowerValueOfRangeException,
  IncorrectUpperValueOfRangeException,
  IncorrectValuesOfRangeException,
  InvalidOwnPaymentInPercentageException,
  LoanAmountContainsNegativeValueException,
  MarginContainsNegativeValueException,
  MissingEnumerationValueException,
  MissingLowerValueOfRangeException,
  MissingSplitPointsListException,
  SplitPointsContainsNegativeValueException
}
import models.{ ConfigFormDto, MarginsFormDto }
import models.RangeType.RangeType

trait FormValidatorBase {
  def validateLowerValueOfRange(lowest: Option[Int]): Either[AppException, Int] =
    lowest
      .toRight(MissingLowerValueOfRangeException)
      .filterOrElse(
        Validator.isGreaterOrEqualToZero,
        IncorrectLowerValueOfRangeException
      )

  def validateUpperValueOfRange(highest: Option[Int]): Either[AppException, Int] =
    highest
      .toRight(MissingLowerValueOfRangeException)
      .filterOrElse(
        Validator.isGreaterOrEqualToZero,
        IncorrectUpperValueOfRangeException
      )

  def validateBothValueOfRange(highest: Int, lowest: Int): Either[AppException, Unit] =
    if (Validator.isUpperGreaterThenLower(highest, lowest))
      Right(())
    else
      Left(IncorrectValuesOfRangeException)

  def validateSplitPoints(splitPoints: Option[Set[Int]]): Either[AppException, Set[Int]] =
    splitPoints
      .toRight(MissingSplitPointsListException)
      .filterOrElse(
        Validator.notContainsNegativeValue,
        SplitPointsContainsNegativeValueException
      )

  def validateRangeType(rangeType: Option[RangeType]): Either[AppException, RangeType] =
    rangeType
      .toRight(MissingEnumerationValueException)

  def validateOwnPayment(form: Option[List[BigDecimal]]): Either[AppException, Unit] = {
    form.map(_.forall(a => Validator.isValidOwnPaymentInPercentage(a))) match {
      case Some(isTrue) if isTrue => Right(())
      case _                      => Left(InvalidOwnPaymentInPercentageException)
    }
  }

  def validateLoanAmount(form: Option[List[BigDecimal]]): Either[AppException, Unit] =
    form.map(a => Validator.notContainsNegative(a)) match {
      case Some(isTrue) if isTrue => Right(())
      case _                      => Left(LoanAmountContainsNegativeValueException)
    }

  def validateMarginValue(form: Option[List[BigDecimal]]): Either[AppException, Unit] =
    form.map(a => Validator.notContainsNegative(a)) match {
      case Some(isTrue) if isTrue => Right(())
      case _                      => Left(MarginContainsNegativeValueException)
    }
}

object FormValidator extends FormValidatorBase {
  def validateConfigForm(f: Option[ConfigFormDto]): Either[AppException, ConfigFormDto] =
    for {
      lowestVal <- validateLowerValueOfRange(f.map(_.lowestValue))
      highestVal <- validateUpperValueOfRange(f.map(_.highestValue))
      _ <- validateBothValueOfRange(highestVal, lowestVal)
      divisions <- validateSplitPoints(f.map(_.splitPoints))
      rangeType <- validateRangeType(f.map(_.rangeType))
    } yield ConfigFormDto(lowestVal, highestVal, divisions, rangeType)

  def validateMarginsForm(f: Option[MarginsFormDto]): Either[AppException, Unit] =
    for {
      _ <- validateOwnPayment(f.map(_.margins.map(_.ownPaymentInPercentage)))
      _ <- validateLoanAmount(f.map(_.margins.map(_.loanAmountPLN)))
      _ <- validateMarginValue(f.map(_.margins.map(_.margin)))
    } yield ()
}
