package commons.repositories

import models.{ BankMargin, Config, ItemDto, Margin, MarginFormDto, MarginsFormDto, Range, RangeType, Settings }
import org.scalatest.concurrent.ScalaFutures.convertScalaFuture
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerTest
import play.api.test.Injecting
import repositories.{ DataBase, MarginRepositoryImpl }

import scala.concurrent.ExecutionContext.Implicits.global

class BankMarginRepositorySpec extends PlaySpec with GuiceOneAppPerTest with Injecting {

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

    val dataBase = new DataBase

    val marginRepository = new MarginRepositoryImpl(dataBase)
  }

  "BankMarginRepository" should {
    "in addMargins" in new TestContext {
      marginRepository.getAll.futureValue mustBe Some(Nil)

      val addedMargins = marginRepository.addMargins(margins).futureValue

      val fromDb = marginRepository.getAll.futureValue

      addedMargins mustBe fromDb
    }

    "in putConfigByRangeType" should {
      "success update own payment settings" in new TestContext {
        val defaultConfig = Config(Settings(0, Set(), 100), Settings(0, Set(), 1000000))
        val config = Config(Settings(0, Set(10, 20, 30, 50), 100), Settings(0, Set(), 1000000))

        marginRepository.getConfig().futureValue mustBe Some(defaultConfig)

        marginRepository.putConfigByRangeType(0, 100, Set(10, 20, 30, 50), RangeType.PAYMENT)

        marginRepository.getConfig().futureValue mustBe Some(config)
      }

      "success update loan amount settings" in new TestContext {
        val defaultConfig = Config(Settings(0, Set(), 100), Settings(0, Set(), 1000000))
        val config = Config(Settings(0, Set(), 100), Settings(0, Set(40000, 80000), 120000))

        marginRepository.getConfig().futureValue mustBe Some(defaultConfig)

        marginRepository.putConfigByRangeType(0, 120000, Set(40000, 80000), RangeType.AMOUNT)

        marginRepository.getConfig().futureValue mustBe Some(config)
      }
    }
  }
}
