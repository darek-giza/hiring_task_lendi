package controllers

import commons.exceptions.AppException.MarginNotFoundException
import models.{ MarginDto, MarginFormDto }
import org.scalamock.scalatest.MockFactory
import org.scalatestplus.play._
import services.MarginService
import org.scalatestplus.play.guice._
import play.api.Play.materializer
import play.api.libs.json.Json
import play.api.test._
import play.api.test.Helpers._

import scala.concurrent.{ ExecutionContext, Future }

class AppControllerSpec(implicit ec: ExecutionContext)
    extends PlaySpec
    with GuiceOneAppPerTest
    with Injecting
    with MockFactory {

  sealed trait TestContext {
    val marginFormDto = MarginFormDto(15, 250000, 55)

    val marginService: MarginService = mock[MarginService]

    val appController = new AppController(stubControllerComponents(), marginService)
  }

  "AppController" should {
    "in GET /getMargin" should {
      "returns exception when missing current loan month" in new TestContext {
        val response = appController
          .getMargin()
          .apply(FakeRequest(GET, "/getMargin"))

        status(response) mustBe 404
        contentType(response) mustBe Some("text/plain")
        contentAsString(response) mustBe "Missing current loan month exception"
      }

      "returns exception when current loan month could not be parsed" in new TestContext {
        val response = appController
          .getMargin()
          .apply(FakeRequest(GET, "/getMargin?currentLoanMonth="))

        status(response) mustBe 404
        contentType(response) mustBe Some("text/plain")
        contentAsString(response) mustBe "Current loan month could not be parsed exception"
      }

      "returns exception when invalid current loan month" in new TestContext {
        val response = appController
          .getMargin()
          .apply(FakeRequest(GET, "/getMargin?currentLoanMonth=-12"))

        status(response) mustBe 404
        contentType(response) mustBe Some("text/plain")
        contentAsString(response) mustBe "Invalid current loan month exception"
      }

      "returns exception when missing loan amountPln " in new TestContext {
        val response = appController
          .getMargin()
          .apply(FakeRequest(GET, "/getMargin?currentLoanMonth=13"))

        status(response) mustBe 404
        contentType(response) mustBe Some("text/plain")
        contentAsString(response) mustBe "Missing loan amountPln exception"
      }

      "returns exception when loan amountPln could not be parsed" in new TestContext {
        val response = appController
          .getMargin()
          .apply(FakeRequest(GET, "/getMargin?currentLoanMonth=13&loanAmountPLN="))

        status(response) mustBe 404
        contentType(response) mustBe Some("text/plain")
        contentAsString(response) mustBe "Loan amountPln could not be parsed exception"
      }

      "returns exception when invalid loan amountPln" in new TestContext {
        val response = appController
          .getMargin()
          .apply(FakeRequest(GET, "/getMargin?currentLoanMonth=13&loanAmountPLN=-1"))

        status(response) mustBe 404
        contentType(response) mustBe Some("text/plain")
        contentAsString(response) mustBe "Invalid loan amountPln exception"
      }

      "returns exception when missing onw payment" in new TestContext {
        val response = appController
          .getMargin()
          .apply(FakeRequest(GET, "/getMargin?currentLoanMonth=13&loanAmountPLN=1000"))

        status(response) mustBe 404
        contentType(response) mustBe Some("text/plain")
        contentAsString(response) mustBe "Missing onw payment in percentage exception"
      }

      "returns exception when onw payment in percentage could not be parsed" in new TestContext {
        val response = appController
          .getMargin()
          .apply(FakeRequest(GET, "/getMargin?currentLoanMonth=13&loanAmountPLN=1000&ownPaymentInPercentage="))

        status(response) mustBe 404
        contentType(response) mustBe Some("text/plain")
        contentAsString(response) mustBe "Onw payment in percentage could not be parsed exception"
      }

      "returns exception when invalid own payment in percentage" in new TestContext {
        val response = appController
          .getMargin()
          .apply(FakeRequest(GET, "/getMargin?currentLoanMonth=13&loanAmountPLN=1000&ownPaymentInPercentage=101"))

        status(response) mustBe 404
        contentType(response) mustBe Some("text/plain")
        contentAsString(response) mustBe "Invalid own payment in percentage exception"
      }

      "returns exception when margin not found" in new TestContext {
        val json = Json.parse("""{"margin":1.125}""")

        (marginService.getMarginDto _)
          .expects(marginFormDto)
          .returns(Future.successful(Left(MarginNotFoundException)))

        val response = appController
          .getMargin()
          .apply(
            FakeRequest(GET, "/getMargin?currentLoanMonth=15&loanAmountPLN=250000&ownPaymentInPercentage=55")
          )

        status(response) mustBe 404
        contentType(response) mustBe Some("text/plain")
        contentAsString(response) mustBe "Margin not found exception"
      }

      "returns MarginDto" in new TestContext {
        val marginDto = MarginDto(BigDecimal("1.125"))
        val json = Json.parse("""{"margin":1.125}""")

        (marginService.getMarginDto _)
          .expects(marginFormDto)
          .returns(Future.successful(Right(marginDto)))

        val response = appController
          .getMargin()
          .apply(
            FakeRequest(GET, "/getMargin?currentLoanMonth=15&loanAmountPLN=250000&ownPaymentInPercentage=55")
          )

        status(response) mustBe OK
        contentType(response) mustBe Some("application/json")
        contentAsJson(response) mustBe json
      }
    }
  }
}
