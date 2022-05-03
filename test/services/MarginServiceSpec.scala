package services

import commons.exceptions.AppException.{ MarginNotFoundException, MarginRangeDoesNotMatchException }
import models.{ Margin, MarginDto, MarginFormDto, Range }
import org.scalamock.scalatest.MockFactory
import org.scalatest.concurrent.ScalaFutures.convertScalaFuture
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerTest
import play.api.test.Injecting
import repositories.MarginRepository

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class MarginServiceSpec extends PlaySpec with GuiceOneAppPerTest with Injecting with MockFactory {

  sealed trait TestContext {
    val margin1 = Margin(Range(10, 20), Range(0, 40000), BigDecimal("3.94"))
    val margin2 = Margin(Range(20, 30), Range(0, 40000), BigDecimal("3.67"))
    val margin3 = Margin(Range(30, 50), Range(0, 40000), BigDecimal("3.54"))

    val margins = List(margin1, margin2, margin3)

    val marginFormDto = MarginFormDto(15, BigDecimal("25000.00"), BigDecimal("25.00"))

    val rangeService = mock[RangeService]
    val marginRepo = mock[MarginRepository]

    val marginService = new MarginServiceImpl(rangeService, marginRepo)
  }

  "MarginService" should {
    "in getMarginDto" should {
      "returns exception when no margin founds" in new TestContext {
        (() => marginRepo.getAll).expects().returns(Future.successful(None))

        val errorOrMargin = marginService.getMarginDto(marginFormDto).futureValue

        errorOrMargin mustBe Left(MarginNotFoundException)
      }

      "returns exception when any margin match to ranges" in new TestContext {
        (() => marginRepo.getAll).expects().returns(Future.successful(Some(margins)))
        (rangeService.matchLoanAmountRange _).expects(margin1, marginFormDto.loanAmountPLN).returns(false)
        (rangeService.matchOwnPaymentRange _).expects(margin1, marginFormDto.ownPaymentInPercentage).returns(false)
        (rangeService.matchLoanAmountRange _).expects(margin2, marginFormDto.loanAmountPLN).returns(false)
        (rangeService.matchOwnPaymentRange _).expects(margin2, marginFormDto.ownPaymentInPercentage).returns(false)
        (rangeService.matchLoanAmountRange _).expects(margin3, marginFormDto.loanAmountPLN).returns(false)
        (rangeService.matchOwnPaymentRange _).expects(margin3, marginFormDto.ownPaymentInPercentage).returns(false)

        val errorOrMargin = marginService.getMarginDto(marginFormDto).futureValue

        errorOrMargin mustBe Left(MarginRangeDoesNotMatchException)
      }

      "returns MarginDto" in new TestContext {
        (() => marginRepo.getAll).expects().returns(Future.successful(Some(margins)))
        (rangeService.matchLoanAmountRange _).expects(margin1, marginFormDto.loanAmountPLN).returns(false)
        (rangeService.matchOwnPaymentRange _).expects(margin1, marginFormDto.ownPaymentInPercentage).returns(false)
        (rangeService.matchLoanAmountRange _).expects(margin2, marginFormDto.loanAmountPLN).returns(true)
        (rangeService.matchOwnPaymentRange _).expects(margin2, marginFormDto.ownPaymentInPercentage).returns(true)

        val errorOrMargin = marginService.getMarginDto(marginFormDto).futureValue

        errorOrMargin mustBe Right(MarginDto(BigDecimal("3.67")))
      }

      "returns MarginDto if current loan month is lower oe equal to 12 month" in new TestContext {
        val formWithCurrentMonthLowerOrEqualToTwelve = marginFormDto.copy(currentLoanMonth = 12)
        val errorOrMargin = marginService.getMarginDto(formWithCurrentMonthLowerOrEqualToTwelve).futureValue

        errorOrMargin mustBe Right(MarginDto(BigDecimal("1.1")))
      }
    }
  }
}
