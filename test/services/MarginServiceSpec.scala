package services

import commons.exceptions.AppException.{
  ConfigNotFoundException,
  IncorrectCreatedMarginCountException,
  MarginNotFoundException,
  MarginRangeDoesNotMatchException
}
import models.{
  BankMargin,
  Config,
  ItemDto,
  Margin,
  MarginDto,
  MarginFormDto,
  MarginsFormDto,
  Range,
  RangesDto,
  Settings
}
import org.scalamock.scalatest.MockFactory
import org.scalatest.concurrent.ScalaFutures.convertScalaFuture
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerTest
import play.api.test.Injecting
import repositories.BankMarginRepository

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class MarginServiceSpec extends PlaySpec with GuiceOneAppPerTest with Injecting with MockFactory {

  sealed trait TestContext {
    val margin1 = Margin(Range(10, 20), Range(0, 40000), BigDecimal("3.94"))
    val margin2 = Margin(Range(20, 30), Range(0, 40000), BigDecimal("3.67"))
    val margin3 = Margin(Range(30, 50), Range(0, 40000), BigDecimal("3.54"))

    val margins = List(margin1, margin2, margin3)

    val item1 = ItemDto(BigDecimal("15"), BigDecimal("25000"), BigDecimal("3.94"))
    val item2 = ItemDto(BigDecimal("25"), BigDecimal("25000"), BigDecimal("3.67"))
    val item3 = ItemDto(BigDecimal("45"), BigDecimal("25000"), BigDecimal("3.54"))

    val marginsFormDto = MarginsFormDto(List(item1, item2, item3))

    val marginFormDto = MarginFormDto(15, BigDecimal("25000.00"), BigDecimal("25.00"))

    val bankMargin = BankMargin(Config(Settings(0, Set.empty, 100), Settings(0, Set.empty, 100000)), Nil)

    val rangeService = mock[RangeService]
    val marginRepo = mock[BankMarginRepository]

    val marginService = new MarginServiceImpl(rangeService, marginRepo)
  }

  "MarginService" should {
    "in getBankMargin" should {
      "returns none when no bank margin found" in new TestContext {
        (() => marginRepo.getBankMargin()).expects().returns(Future.successful(None))

        val errorOrMargin = marginService.getBankMargin().futureValue

        errorOrMargin mustBe None
      }

      "returns bank margin" in new TestContext {
        (() => marginRepo.getBankMargin()).expects().returns(Future.successful(Some(bankMargin)))

        val errorOrMargin = marginService.getBankMargin().futureValue

        errorOrMargin mustBe Some(bankMargin)
      }
    }

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

    "in putMargins" should {
      "returns ex when config not found" in new TestContext {

        (() => rangeService.getAll()).expects().returns(Future.successful(Left(ConfigNotFoundException)))

        val errorOrMargin = marginService.putMargins(marginsFormDto).futureValue

        errorOrMargin mustBe Left(ConfigNotFoundException)
      }

      "returns ex when count created margin is incorrect" in new TestContext {
        val listOwnPay = List(Range(20, 30), Range(30, 50))
        val listLoanAm = List(Range(0, 40000))
        val rangesDto = RangesDto(listOwnPay, listLoanAm)

        (() => rangeService.getAll()).expects().returns(Future.successful(Right(rangesDto)))
        (rangeService.findRange _).expects(listOwnPay, BigDecimal("15")).returns(None)
        (rangeService.findRange _).expects(listLoanAm, BigDecimal("25000")).returns(Some(Range(0, 40000)))

        val errorOrMargin = marginService.putMargins(MarginsFormDto(List(item1))).futureValue

        errorOrMargin mustBe Left(IncorrectCreatedMarginCountException)
      }

      "returns ex when margin not found in repository" in new TestContext {
        val listOwnPay = List(Range(0, 30), Range(30, 50))
        val listLoanAm = List(Range(0, 40000))
        val rangesDto = RangesDto(listOwnPay, listLoanAm)

        (() => rangeService.getAll()).expects().returns(Future.successful(Right(rangesDto)))

        (marginRepo.addMargins _).expects(Nil).returns(Future.successful(None))

        val errorOrMargin = marginService.putMargins(MarginsFormDto(Nil)).futureValue

        errorOrMargin mustBe Left(MarginNotFoundException)
      }

      "returns correctly created margins" in new TestContext {
        val listOwnPay = List(Range(10, 20), Range(20, 30), Range(30, 50))
        val listLoanAm = List(Range(0, 40000))
        val rangesDto = RangesDto(listOwnPay, listLoanAm)

        (() => rangeService.getAll()).expects().returns(Future.successful(Right(rangesDto)))
        (rangeService.findRange _).expects(listOwnPay, BigDecimal("15")).returns(Some(Range(10, 20)))
        (rangeService.findRange _).expects(listLoanAm, BigDecimal("25000")).returns(Some(Range(0, 40000)))

        (rangeService.findRange _).expects(listOwnPay, BigDecimal("25")).returns(Some(Range(20, 30)))
        (rangeService.findRange _).expects(listLoanAm, BigDecimal("25000")).returns(Some(Range(0, 40000)))

        (rangeService.findRange _).expects(listOwnPay, BigDecimal("45")).returns(Some(Range(30, 50)))
        (rangeService.findRange _).expects(listLoanAm, BigDecimal("25000")).returns(Some(Range(0, 40000)))

        (marginRepo.addMargins _).expects(margins).returns(Future.successful(Some(List(margin1, margin2, margin3))))

        val errorOrMargin = marginService.putMargins(marginsFormDto).futureValue

        errorOrMargin mustBe Right(List(margin1, margin2, margin3))
      }
    }

  }
}
