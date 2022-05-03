package commons.validator

import commons.exceptions.AppException
import commons.exceptions.AppException._
import models.MarginFormDto

import scala.util.Try

trait ParamsValidatorBase {
  def validateCurrentLoanMonth(monthOpt: Option[String]): Either[AppException, Int] =
    monthOpt
      .toRight(MissingCurrentLoanMonthException)
      .flatMap { month =>
        Try(month.toInt).toEither.left.map(_ => CurrentLoanMonthCouldNotBeParsedException)
      }
      .filterOrElse(
        Validator.isValidCurrentLoanMonth,
        InvalidCurrentLoanMonthException
      )

  def validateLoanAmountPLN(amountOpt: Option[String]): Either[AppException, BigDecimal] =
    amountOpt
      .toRight(MissingLoanAmountPlnException)
      .flatMap { amount =>
        Try(BigDecimal(amount).setScale(2, BigDecimal.RoundingMode.HALF_UP)).toEither.left
          .map(_ => LoanAmountPlnCouldNotBeParsedException)
      }
      .filterOrElse(
        Validator.isValidLoanAmountPln,
        InvalidLoanAmountPlnException
      )

  def validateOwnPaymentInPercentage(percentageOpt: Option[String]): Either[AppException, BigDecimal] =
    percentageOpt
      .toRight(MissingOwnPaymentInPercentageException)
      .flatMap { percentage =>
        Try(BigDecimal(percentage).setScale(2, BigDecimal.RoundingMode.HALF_UP)).toEither.left
          .map(_ => OwnPaymentInPercentageCouldNotBeParsedException)
      }
      .filterOrElse(
        Validator.isValidOwnPaymentInPercentage,
        InvalidOwnPaymentInPercentageException
      )
}

object ParamsValidator extends ParamsValidatorBase {

  def validateParams(
      month: Option[String],
      amount: Option[String],
      payment: Option[String]
  ): Either[AppException, MarginFormDto] = {
    for {
      month <- validateCurrentLoanMonth(month)
      amount <- validateLoanAmountPLN(amount)
      percentage <- validateOwnPaymentInPercentage(payment)
    } yield MarginFormDto(month, amount, percentage)
  }
}
