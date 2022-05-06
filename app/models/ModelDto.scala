package models

import models.RangeType.RangeType
import play.api.libs.json.Json

case class MarginFormDto(currentLoanMonth: Int, loanAmountPLN: BigDecimal, ownPaymentInPercentage: BigDecimal)
case class ConfigFormDto(lowestValue: Int, highestValue: Int, splitPoints: Set[Int], rangeType: RangeType)
case class RangesDto(ownPayment: List[Range], loanAmount: List[Range])
case class MarginDto(margin: BigDecimal)
case class ItemDto(ownPaymentInPercentage: BigDecimal, loanAmountPLN: BigDecimal, margin: BigDecimal)
case class MarginsFormDto(margins: List[ItemDto])

object RangeType extends Enumeration {
  implicit val rangeTypeEncoder = Json.formatEnum(RangeType)

  type RangeType = Value
  val AMOUNT, PAYMENT = Value
}

object MarginFormDto {
  implicit val marginFormDtoFormat = Json.format[MarginFormDto]
}

object ConfigFormDto {
  implicit val configFormDtoFormat = Json.format[ConfigFormDto]
}

object MarginDto {
  implicit val marginDtoFormat = Json.format[MarginDto]
}

object ItemDto {
  implicit val itemDtoFormat = Json.format[ItemDto]
}

object MarginsFormDto {
  implicit val marginsFormDtoFormat = Json.format[MarginsFormDto]
}

object RangesDto {
  implicit val rangesDtoFormat = Json.format[RangesDto]
}
