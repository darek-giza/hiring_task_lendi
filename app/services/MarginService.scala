package services

import cats.data.EitherT
import commons.exceptions.AppException
import commons.exceptions.AppException.{ MarginNotFoundException, MarginRangeDoesNotMatchException }
import models.{ Margin, MarginDto, MarginFormDto }
import repositories.MarginRepository

import javax.inject.Inject
import scala.concurrent.{ ExecutionContext, Future }

trait MarginService {
  def getMarginDto(form: MarginFormDto): Future[Either[AppException, MarginDto]]
}

class MarginServiceImpl @Inject()(rangeService: RangeService, marginRepository: MarginRepository)(
    implicit ec: ExecutionContext
) extends MarginService {

  val CONSTANT_MARGIN_DURING_FIRST_TWELVE_MONTHS = BigDecimal("1.1")
  val COUNT_OF_MONTH_WITH_LOWER_MARGIN = 12

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
}
