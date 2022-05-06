package controllers

import models.{
  BankMargin,
  Config,
  ConfigFormDto,
  ItemDto,
  Margin,
  MarginDto,
  MarginFormDto,
  MarginsFormDto,
  Range,
  RangeType,
  RangesDto,
  Settings
}
import org.scalamock.scalatest.MockFactory
import org.scalatestplus.play._
import services.{ MarginService, RangeService }
import org.scalatestplus.play.guice._
import play.api.Play.materializer
import play.api.libs.json.{ JsResult, JsValue, Json }
import play.api.test._
import play.api.test.Helpers._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class AppControllerSpec extends PlaySpec with GuiceOneAppPerTest with Injecting with MockFactory {

  sealed trait TestContext {
    val item1 = ItemDto(BigDecimal("15"), BigDecimal("25000"), BigDecimal("3.9"))
    val item2 = ItemDto(BigDecimal("40"), BigDecimal("25000"), BigDecimal("3.7"))
    val item3 = ItemDto(BigDecimal("70"), BigDecimal("25000"), BigDecimal("3.5"))

    val marginsFormDto = MarginsFormDto(List(item1, item2, item3))

    val margin1 = Margin(Range(10, 20), Range(0, 40000), BigDecimal("3.9"))
    val margin2 = Margin(Range(20, 50), Range(0, 40000), BigDecimal("3.7"))
    val margin3 = Margin(Range(50, 100), Range(0, 40000), BigDecimal("3.5"))

    val listMargin = List(margin1, margin2, margin3)

    val marginFormDto = MarginFormDto(15, 250000, 55)

    val marginService = mock[MarginService]
    val rangeService = mock[RangeService]

    val appController = new AppController(stubControllerComponents(), marginService, rangeService)
  }

  "AppController" should {
    "in GET /margin-get" should {
      "returns MarginDto" in new TestContext {
        val marginDto = MarginDto(BigDecimal("1.125"))
        val json = Json.parse("""{"margin":1.125}""")

        (marginService.getMarginDto _)
          .expects(marginFormDto)
          .returns(Future.successful(Right(marginDto)))

        val response = appController
          .getMargin()
          .apply(
            FakeRequest(GET, "/margin-get?currentLoanMonth=15&loanAmountPLN=250000&ownPaymentInPercentage=55")
          )

        status(response) mustBe OK
        contentType(response) mustBe Some("application/json")
        contentAsJson(response) mustBe json
      }
    }

    "in PUT /margin-add" should {
      "returns list margins" in new TestContext {

        (marginService.putMargins _).expects(marginsFormDto).returns(Future.successful(Right(listMargin)))

        val jsonString =
          """{"margins":
            |[
            |{"ownPaymentInPercentage":15,"loanAmountPLN":25000,"margin":3.9},
            |{"ownPaymentInPercentage":40,"loanAmountPLN":25000,"margin":3.7},
            |{"ownPaymentInPercentage":70,"loanAmountPLN":25000,"margin":3.5}
            |]
            |}""".stripMargin

        val response = appController
          .addMargins()
          .apply(
            FakeRequest(PUT, "/margin-add")
              .withJsonBody(Json.parse(jsonString))
          )

        status(response) mustBe OK
        contentType(response) mustBe Some("application/json")
        contentAsJson(response) mustBe
          Json.parse(
            """
              |[
              |{"ownPaymentRangeInPercent":{"min":10,"max":20},"loanAmountRangePLN":{"min":0,"max":40000},"value":3.9},
              |{"ownPaymentRangeInPercent":{"min":20,"max":50},"loanAmountRangePLN":{"min":0,"max":40000},"value":3.7},
              |{"ownPaymentRangeInPercent":{"min":50,"max":100},"loanAmountRangePLN":{"min":0,"max":40000},"value":3.5}
              |]
              |""".stripMargin
          )
      }
    }

    "in PUT /range-update" should {
      "returns list margins" in new TestContext {
        val configFormDto = ConfigFormDto(0, 100, Set(10, 20, 30, 50), RangeType.PAYMENT)
        val config = Config(Settings(0, Set(10, 20, 30, 50), 100), Settings(0, Set(40000), 80000))

        (rangeService.updateRange _).expects(configFormDto).returns(Future.successful(Some(config)))

        val jsonString = """{"lowestValue":0,"highestValue":100,"splitPoints":[10,20,30,50],"rangeType":"PAYMENT"}"""

        val response = appController
          .addMargins()
          .apply(
            FakeRequest(PUT, "/range-update")
              .withJsonBody(Json.parse(jsonString))
          )

        status(response) mustBe OK
        contentType(response) mustBe Some("application/json")
        contentAsJson(response) mustBe Json.parse(" ")
      }
    }

    "in GET /range-get" should {
      "returns list of own payment and loan amount" in new TestContext {
        val rangesDto = RangesDto(List(Range(0, 20), Range(20, 100)), List(Range(0, 40000), Range(40000, 80000)))

        (() => rangeService.getAll()).expects().returns(Future.successful(Right(rangesDto)))

        val response = appController
          .getRanges()
          .apply(
            FakeRequest(GET, "/range-get")
          )

        status(response) mustBe OK
        contentType(response) mustBe Some("application/json")
        contentAsJson(response) mustBe Json.parse(
          """{
            |"ownPayment":[
            |{"min":0,"max":20},{"min":20,"max":100}
            |],
            |"loanAmount":[
            |{"min":0,"max":40000},{"min":40000,"max":80000}
            |]
            |}""".stripMargin
        )
      }
    }

    "in GET /bank-margin-get" should {
      "returns config and margin ranges" in new TestContext {

        val bankMargin =
          BankMargin(Config(Settings(10, Set(50), 100), Settings(0, Set(), 40000)), List(margin1, margin2, margin3))

        (() => marginService.getBankMargin()).expects().returns(Future.successful(Some(bankMargin)))

        val response = appController
          .getBankMargin()
          .apply(
            FakeRequest(GET, "/bank-margin-get")
          )

        status(response) mustBe OK
        contentType(response) mustBe Some("application/json")
        contentAsJson(response) mustBe Json.parse(
          """
            |{"config":
            |{"ownPayment":{"lowestValue":10,"splitPoints":[50],"highestValue":100},
            |"loanAmount":{"lowestValue":0,"splitPoints":[],"highestValue":40000}},
            |"margins":
            |[
            |{"ownPaymentRangeInPercent":{"min":10,"max":20},"loanAmountRangePLN":{"min":0,"max":40000},"value":3.9},
            |{"ownPaymentRangeInPercent":{"min":20,"max":50},"loanAmountRangePLN":{"min":0,"max":40000},"value":3.7},
            |{"ownPaymentRangeInPercent":{"min":50,"max":100},"loanAmountRangePLN":{"min":0,"max":40000},"value":3.5}
            |]
            |}""".stripMargin
        )
      }
    }
  }
}
