package models

import play.api.libs.json._

case class Range(min: Int, max: Int)
case class Settings(lowestValue: Int, splitPoints: Set[Int], highestValue: Int)
case class Config(ownPayment: Settings, loanAmount: Settings)
case class Margin(ownPaymentRangeInPercent: Range, loanAmountRangePLN: Range, value: BigDecimal)
case class BankMargin(config: Config, margins: List[Margin])

object Range {
  implicit val rangeFormat = Json.format[Range]
}

object Settings {
  implicit val settingsFormat = Json.format[Settings]
}

object Config {
  implicit val configFormat = Json.format[Config]
}

object Margin {
  implicit val marginFormat = Json.format[Margin]
}

object BankMargin {
  implicit val bankMarginFormat = Json.format[BankMargin]
}
