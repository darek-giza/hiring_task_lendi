package services

import cats.data.EitherT
import cats.implicits._
import commons.exceptions.AppException
import commons.exceptions.AppException.{
  IncorrectCreatedMarginCountException,
  MarginNotFoundException,
  MarginRangeDoesNotMatchException
}
import models.{ BankMargin, ItemDto, Margin, MarginDto, MarginFormDto, MarginsFormDto, RangesDto }
import repositories.BankMarginRepository

import javax.inject.Inject
import scala.annotation.tailrec
import scala.concurrent.{ ExecutionContext, Future }

trait MarginService {
  def getMarginDto(form: MarginFormDto): Future[Either[AppException, MarginDto]]
  def putMargins(form: MarginsFormDto): Future[Either[AppException, List[Margin]]]
  def getBankMargin(): Future[Option[BankMargin]]
}

class MarginServiceImpl @Inject()(rangeService: RangeService, marginRepository: BankMarginRepository)(
    implicit ec: ExecutionContext
) extends MarginService {

  val CONSTANT_MARGIN_DURING_FIRST_TWELVE_MONTHS = BigDecimal("1.1")
  val COUNT_OF_MONTH_WITH_LOWER_MARGIN = 12

  override def getBankMargin(): Future[Option[BankMargin]] =
    marginRepository.getBankMargin()

  def getMarginDto(form: MarginFormDto): Future[Either[AppException, MarginDto]] = {
    def takeLowerMarginValue =
      Future.successful(Right(MarginDto(CONSTANT_MARGIN_DURING_FIRST_TWELVE_MONTHS)))

    def findMatchingMargin(margins: List[Margin]) =
      Future.successful(margins.find(margin => {
        val isAmountInRange = rangeService.matchLoanAmountRange(margin, form.loanAmountPLN)
        val isOwnPaymentInRange = rangeService.matchOwnPaymentRange(margin, form.ownPaymentInPercentage)
        isAmountInRange && isOwnPaymentInRange
      }))

    def takeBaseMarginValue = {
      (for {
        allMargin <- EitherT.fromOptionF(marginRepository.getAll, MarginNotFoundException)
        margin <- EitherT.fromOptionF(findMatchingMargin(allMargin), MarginRangeDoesNotMatchException: AppException)
      } yield MarginDto(margin.value)).value
    }

    if (form.currentLoanMonth <= COUNT_OF_MONTH_WITH_LOWER_MARGIN)
      takeLowerMarginValue
    else takeBaseMarginValue
  }

  override def putMargins(form: MarginsFormDto): Future[Either[AppException, List[Margin]]] = {

    @tailrec
    def createMargins(items: List[ItemDto], ranges: RangesDto, created: List[Margin]): Future[List[Margin]] = {
      if (items.isEmpty)
        Future.successful(created)
      else {
        val ownPayment = items.head.ownPaymentInPercentage
        val loanAmount = items.head.loanAmountPLN

        def ownPaymentRange = rangeService.findRange(ranges.ownPayment, ownPayment)
        def loanAmountRange = rangeService.findRange(ranges.loanAmount, loanAmount)

        val newOrEmpty = (ownPaymentRange, loanAmountRange) match {
          case (Some(payment), Some(amount)) => List(Margin(payment, amount, items.head.margin))
          case (_, _)                        => Nil
        }

        createMargins(items.tail, ranges, created ++ newOrEmpty)
      }
    }

    (for {
      ranges <- EitherT(rangeService.getAll())
      margins <- EitherT.liftF[Future, AppException, List[Margin]](createMargins(form.margins, ranges, Nil))
      itemsSize = form.margins.size
      _ <- EitherT.cond[Future](margins.size == itemsSize, (), IncorrectCreatedMarginCountException)
      saved <- EitherT.fromOptionF(marginRepository.addMargins(margins), MarginNotFoundException: AppException)
    } yield saved).value
  }
}
