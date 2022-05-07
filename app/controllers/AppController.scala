package controllers

import com.google.inject.Inject
import commons.exceptions.AppException
import commons.validator.{ FormValidator, ParamsValidator }
import models.{ ConfigFormDto, MarginFormDto, MarginsFormDto }
import play.api.libs.json.Format.GenericFormat
import play.api.libs.json.Json
import play.api.mvc._
import services.{ MarginService, RangeService }

import scala.concurrent.{ ExecutionContext, Future }

class AppController @Inject()(
    val controllerComponents: ControllerComponents,
    marginService: MarginService,
    rangeService: RangeService
)(
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

  def addMargins() = Action.async { implicit request =>
    val formOpt = request.body.asJson.map(_.as[MarginsFormDto])

    println(formOpt)

    def add(form: Option[MarginsFormDto]) =
      form
        .map(
          f =>
            marginService.putMargins(f).map {
              case Right(list) => Ok(Json.toJson(list))
              case Left(ex)    => exceptionAsResult(ex)
            }
        )
        .getOrElse(Future.successful(BadRequest))

    FormValidator.validateMarginsForm(formOpt) match {
      case Right(_) => add(formOpt)
      case Left(ex) => Future.successful(exceptionAsResult(ex))
    }
  }

  def getRanges() = Action.async { implicit request =>
    rangeService.getAll().map {
      case Right(rangesDto) => Ok(Json.toJson(rangesDto))
      case Left(ex)         => exceptionAsResult(ex)
    }
  }

  def getBankMargin() = Action.async { implicit request =>
    marginService.getBankMargin().map {
      case Some(value) => Ok(Json.toJson(value))
      case _           => BadRequest
    }
  }

  def updateRange() = Action.async { implicit request =>
    val formOpt = request.body.asJson.map(_.as[ConfigFormDto])

    def update(form: ConfigFormDto) = rangeService.updateRange(form).map {
      case Some(value) => Ok(Json.toJson(value))
      case _           => BadRequest
    }

    FormValidator.validateConfigForm(formOpt) match {
      case Right(form) => update(form)
      case Left(ex)    => Future.successful(exceptionAsResult(ex))
    }
  }

  def redirectDocs = Action {
    Redirect(url = "/assets/lib/swagger-ui/index.html", queryStringParams = Map("url" -> Seq("/docs/swagger.yml")))
  }

  private def exceptionAsResult(ex: AppException): Result =
    Status(ex.statusCode).apply(ex.message)
}
