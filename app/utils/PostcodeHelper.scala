package utils

import uk.gov.hmrc.address.uk.Postcode

object PostcodeHelper {

  def displayPostcode(p:Option[String]) = p.flatMap(Postcode.cleanupPostcode).map(_.toString).getOrElse("")
  def displayPostcode(p:String) = Postcode.cleanupPostcode(p).map(_.toString).getOrElse("")
}