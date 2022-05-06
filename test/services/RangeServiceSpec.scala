package services

import commons.exceptions.AppException.ConfigNotFoundException
import models.{ Config, ConfigFormDto, Margin, Range, RangeType, RangesDto, Settings }
import org.scalamock.scalatest.MockFactory
import org.scalatest.concurrent.ScalaFutures.convertScalaFuture
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerTest
import repositories.BankMarginRepository

import scala.collection.SortedSet
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class RangeServiceSpec extends PlaySpec with GuiceOneAppPerTest with MockFactory {

  sealed trait TestContext {
    val margin = Margin(Range(10, 20), Range(0, 40000), BigDecimal(3.49))

    val marginRepo = mock[BankMarginRepository]

    val rangeService = new RangeServiceImpl(marginRepo)
  }

  "RangeService" should {
    "in findRange" should {
      "return none when nothing match" in new TestContext {
        val listRanges = List(Range(10, 20), Range(20, 30), Range(30, 50), Range(50, 100))

        rangeService.findRange(listRanges, BigDecimal("5")) mustBe None
      }

      "return range which contains this value " in new TestContext {
        val listRanges = List(Range(10, 20), Range(20, 30), Range(30, 50), Range(50, 100))

        rangeService.findRange(listRanges, BigDecimal("35")) mustBe Some(Range(30, 50))
      }
    }

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
        rangeService.matchLoanAmountRange(margin, BigDecimal("39999.99")) mustBe true
      }
    }

    "in getAll" should {
      "returns ex when config not found" in new TestContext {
        (() => marginRepo.getConfig()).expects().returns(Future.successful(None))

        val errorOrRanges = rangeService.getAll().futureValue

        errorOrRanges mustBe Left(ConfigNotFoundException)

      }

      "returns list all default ranges" in new TestContext {
        val config = Config(Settings(0, Set.empty, 100), Settings(0, Set.empty, 100000))
        (() => marginRepo.getConfig()).expects().returns(Future.successful(Some(config)))

        val errorOrRanges = rangeService.getAll().futureValue

        errorOrRanges mustBe Right(RangesDto(List(Range(0, 100)), List(Range(0, 100000))))
      }

      "returns list all ranges" in new TestContext {
        val config = Config(Settings(10, Set(20, 30, 50), 100), Settings(0, Set(40000, 80000), 120000))
        (() => marginRepo.getConfig()).expects().returns(Future.successful(Some(config)))

        val errorOrRanges = rangeService.getAll().futureValue

        errorOrRanges mustBe Right(
          RangesDto(
            List(Range(10, 20), Range(20, 30), Range(30, 50), Range(50, 100)),
            List(Range(0, 40000), Range(40000, 80000), Range(80000, 120000))
          )
        )
      }
    }

    "in generateRanges" should {
      "return one range from lowest to highest value" in new TestContext {

        val listRange = rangeService.generateRanges(Set.empty, 0, 100).futureValue

        listRange mustBe List(Range(0, 100))
      }

      "should generate list of Ranges" in new TestContext {
        val created = rangeService.generateRanges(Set(10, 20, 30, 50), 0, 100).futureValue

        created mustBe List(Range(0, 10), Range(10, 20), Range(20, 30), Range(30, 50), Range(50, 100))

      }
    }

    "in sanitizeSplitPoints" should {
      "returns empty list" in new TestContext {
        val lowestVal = 0
        val highestVal = 100
        val splitPoints: Set[Int] = Set.empty

        val sanitized = rangeService.sanitizeSplitPoints(splitPoints, lowestVal, highestVal).futureValue

        sanitized mustBe Set.empty
      }

      "returns sorted set" in new TestContext {
        val lowestVal = 0
        val highestVal = 100
        val splitPoints = Set(50, 10, 20, 30)

        val sanitized = rangeService.sanitizeSplitPoints(splitPoints, lowestVal, highestVal).futureValue

        sanitized mustBe Set(10, 20, 30, 50)
      }

      "returns sorted list starting with 20" in new TestContext {
        val lowestVal = 20
        val highestVal = 100
        val splitPoints = Set(50, 10, 20, 30)

        val sanitized = rangeService.sanitizeSplitPoints(splitPoints, lowestVal, highestVal).futureValue

        sanitized mustBe Set(20, 30, 50)
      }
    }

    "in updateRange" should {
      "failed when not updated config" in new TestContext {
        val configForm = ConfigFormDto(0, 100, Set.empty, RangeType.PAYMENT)

        (marginRepo.putConfigByRangeType _)
          .expects(0, 100, Set[Int](), RangeType.PAYMENT)
          .returns(Future.successful(None))

        val configOpt = rangeService.updateRange(configForm).futureValue

        configOpt mustBe None
      }

      "success when updated own payment settings in config" in new TestContext {
        val configForm = ConfigFormDto(10, 100, Set(20, 30, 50), RangeType.PAYMENT)
        val config = Config(Settings(10, Set(20, 30, 50), 100), Settings(0, Set.empty, 40000))

        (marginRepo.putConfigByRangeType _)
          .expects(10, 100, Set(20, 30, 50), RangeType.PAYMENT)
          .returns(Future.successful(Some(config)))

        val configOpt = rangeService.updateRange(configForm).futureValue

        configOpt mustBe Some(config)
      }

      "success when updated loan amount settings in config" in new TestContext {
        val configForm = ConfigFormDto(0, 120000, Set(40000, 80000), RangeType.AMOUNT)
        val config = Config(Settings(10, Set(20, 30, 50), 100), Settings(0, Set(40000, 80000), 120000))

        (marginRepo.putConfigByRangeType _)
          .expects(0, 120000, Set(40000, 80000), RangeType.AMOUNT)
          .returns(Future.successful(Some(config)))

        val configOpt = rangeService.updateRange(configForm).futureValue

        configOpt mustBe Some(config)
      }
    }
  }
}
