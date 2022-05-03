package repositories

import models.{ Margin, Range }

import scala.concurrent.Future

trait MarginRepository {
  def getAll: Future[Option[List[Margin]]]
}

class MarginRepositoryImpl extends MarginRepository {
  val margin11 = Margin(Range(10, 20), Range(0, 40000), BigDecimal("3.94"))
  val margin12 = Margin(Range(20, 30), Range(0, 40000), BigDecimal("3.67"))
  val margin13 = Margin(Range(30, 50), Range(0, 40000), BigDecimal("3.54"))
  val margin14 = Margin(Range(50, 100), Range(0, 40000), BigDecimal("2.94"))

  val margin21 = Margin(Range(10, 20), Range(40000, 80000), BigDecimal("2.49"))
  val margin22 = Margin(Range(20, 30), Range(40000, 80000), BigDecimal("2.32"))
  val margin23 = Margin(Range(30, 50), Range(40000, 80000), BigDecimal("2.29"))
  val margin24 = Margin(Range(50, 100), Range(40000, 80000), BigDecimal("2.18"))

  val margins = List(margin11, margin12, margin13, margin14, margin21, margin22, margin23, margin24)

  override def getAll = Future.successful(Some(margins))
}
