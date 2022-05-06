package services

import cats.data.EitherT
import cats.implicits._
import com.google.inject.Inject
import commons.exceptions.AppException
import commons.exceptions.AppException.ConfigNotFoundException
import models.{ Config, Margin, Range, ConfigFormDto, RangesDto }
import repositories.BankMarginRepository

import scala.annotation.tailrec
import scala.collection.SortedSet
import scala.concurrent.{ ExecutionContext, Future }

trait RangeService {
  def matchLoanAmountRange(margin: Margin, amount: BigDecimal): Boolean
  def matchOwnPaymentRange(margin: Margin, payment: BigDecimal): Boolean
  def findRange(ranges: List[Range], payment: BigDecimal): Option[Range]
  def generateRanges(divisions: Set[Int], lowestValue: Int, upperValue: Int): Future[List[Range]]
  def updateRange(form: ConfigFormDto): Future[Option[Config]]
  def sanitizeSplitPoints(list: Set[Int], lower: Int, upper: Int): Future[Set[Int]]
  def getAll(): Future[Either[AppException, RangesDto]]
}

class RangeServiceImpl @Inject()(marginRepository: BankMarginRepository)(implicit ec: ExecutionContext)
    extends RangeService {

  override def findRange(ranges: List[Range], param: BigDecimal): Option[Range] =
    ranges.find(r => param >= r.min && param <= r.max)

  override def matchOwnPaymentRange(m: Margin, payment: BigDecimal): Boolean =
    payment >= m.ownPaymentRangeInPercent.min && payment < m.ownPaymentRangeInPercent.max

  override def matchLoanAmountRange(m: Margin, amount: BigDecimal): Boolean =
    amount >= m.loanAmountRangePLN.min && amount < m.loanAmountRangePLN.max

  override def getAll(): Future[Either[AppException, RangesDto]] = {
    (for {
      config <- EitherT.fromOptionF(marginRepository.getConfig(), ConfigNotFoundException)
      cla = config.loanAmount
      cop = config.ownPayment
      amountRanges <- EitherT.liftF[Future, AppException, List[Range]](
        generateRanges(cla.splitPoints, cla.lowestValue, cla.highestValue)
      )
      paymentRanges <- EitherT.liftF[Future, AppException, List[Range]](
        generateRanges(cop.splitPoints, cop.lowestValue, cop.highestValue)
      )
    } yield RangesDto(paymentRanges, amountRanges)).value
  }

  override def generateRanges(splitPoints: Set[Int], lowestValue: Int, highestValue: Int): Future[List[Range]] = {
    val withLowestValue = List(lowestValue) ++ splitPoints

    @tailrec
    def create(div: List[Int], ranges: List[Range]): List[Range] = {
      if (div.isEmpty)
        ranges
      else {
        val tail =
          if (div.tail.isEmpty)
            List(highestValue)
          else
            div.tail

        val updatedRanges = ranges ++ List(Range(div.head, tail.head))

        create(div.tail, updatedRanges)
      }
    }
    Future.successful(create(withLowestValue, Nil))
  }

  override def sanitizeSplitPoints(splitPoints: Set[Int], lowestVal: Int, highestVal: Int): Future[Set[Int]] = {
    val sanitized = splitPoints.filter(d => d >= lowestVal && d < highestVal)

    Future.successful(
      (SortedSet.empty[Int] ++ sanitized).toSet
    )
  }

  override def updateRange(f: ConfigFormDto): Future[Option[Config]] =
    for {
      sanitizedPoints <- sanitizeSplitPoints(f.splitPoints, f.lowestValue, f.highestValue)
      configOpt <- marginRepository.putConfigByRangeType(f.lowestValue, f.highestValue, sanitizedPoints, f.rangeType)
    } yield configOpt
}
