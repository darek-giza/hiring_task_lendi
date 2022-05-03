package models

import play.api.libs.json._

case class Range(min: Int, max: Int)

case class Margin(ownPaymentRangeInPercentage: Range, loanAmountRangePLN: Range, value: BigDecimal)

case class Margins(marginRanges: List[Margin])

case class MarginFormDto(currentLoanMonth: Int, loanAmountPLN: BigDecimal, ownPaymentInPercentage: BigDecimal)

object MarginFormDto {
  implicit val marginFormDtoFormat = Json.format[MarginFormDto]
}

case class MarginDto(margin: BigDecimal)

object MarginDto {
  implicit val marginDtoFormat = Json.format[MarginDto]
}
