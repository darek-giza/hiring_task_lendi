package controllers

import com.google.inject.Inject
import commons.exceptions.AppException
import commons.validator.ParamsValidator
import models.MarginFormDto
import play.api.libs.json.Json
import play.api.mvc._
import services.MarginService

import scala.concurrent.{ ExecutionContext, Future }

class AppController @Inject()(val controllerComponents: ControllerComponents, marginService: MarginService)(
    implicit ec: ExecutionContext
) extends BaseController {

  def getMargin() = Action.async { implicit request =>
    def takeMarginValue(validForm: MarginFormDto): Future[Result] =
      marginService.getMarginDto(validForm).map {
        case Right(value) => Ok(Json.toJson(value))
        case Left(ex)     => exceptionAsResult(ex)
      }

    val monthOpt = request.getQueryString("currentLoanMonth")
    val amountOpt = request.getQueryString("loanAmountPLN")
    val ownPaymentOpt = request.getQueryString("ownPaymentInPercentage")

    ParamsValidator.validateParams(monthOpt, amountOpt, ownPaymentOpt) match {
      case Right(validForm) => takeMarginValue(validForm)
      case Left(ex)         => Future.successful(exceptionAsResult(ex))
    }
  }

  private def exceptionAsResult(ex: AppException): Result =
    Status(ex.statusCode).apply(ex.message)
}
