package repositories

import com.google.inject.Inject
import models.RangeType.{ AMOUNT, PAYMENT, RangeType }
import models.{ BankMargin, Config, Margin, Settings }

import scala.concurrent.{ ExecutionContext, Future }

trait BankMarginRepository {
  def getAll: Future[Option[List[Margin]]]
  def addMargins(margins: List[Margin]): Future[Option[List[Margin]]]
  def getConfig(): Future[Option[Config]]
  def getBankMargin(): Future[Option[BankMargin]]
  def putConfigByRangeType(
      lowestValue: Int,
      highestValue: Int,
      splitPoints: Set[Int],
      rangeType: RangeType
  ): Future[Option[Config]]
}

class MarginRepositoryImpl @Inject()(dataBase: DataBase)(implicit ec: ExecutionContext) extends BankMarginRepository {

  val key = "pkobp"
  val DB = dataBase.db

  override def addMargins(margins: List[Margin]): Future[Option[List[Margin]]] = {
    val bankMargin = DB.computeIfPresent(
      key,
      (key, old) => {
        val config = old.config
        BankMargin(config, margins)
      }
    )
    Future.successful(
      Some(bankMargin.margins)
    )
  }

  override def getAll =
    Future.successful(
      Some(DB.get(key).margins)
    )

  override def getBankMargin(): Future[Option[BankMargin]] =
    Future.successful(
      Some(DB.get(key))
    )

  override def getConfig(): Future[Option[Config]] =
    Future.successful(
      Some(DB.get(key).config)
    )

  override def putConfigByRangeType(
      lowestValue: Int,
      highestValue: Int,
      splitPoints: Set[Int],
      rangeType: RangeType
  ): Future[Option[Config]] = {
    val newSettings = Settings(lowestValue, splitPoints, highestValue)

    val oldBankMargin = DB.computeIfPresent(
      key,
      (key, old) => {
        old
      }
    )

    val newBankMargin = rangeType match {
      case PAYMENT => BankMargin(Config(newSettings, oldBankMargin.config.loanAmount), Nil)
      case AMOUNT  => BankMargin(Config(oldBankMargin.config.ownPayment, newSettings), Nil)
    }

    val isSuccess = DB.replace(key, oldBankMargin, newBankMargin)

    if (isSuccess)
      Future.successful(Some(newBankMargin.config))
    else Future.successful(None)
  }
}
