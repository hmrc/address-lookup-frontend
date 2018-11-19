package utils

import uk.gov.hmrc.address.uk.Postcode

object PostcodeHelper {

  def displayPostcode(p:Option[String]) = p.fold("")(pc => Postcode.cleanupPostcode(pc).fold("")(_.toString))
  def displayPostcode(p:String) = Postcode.cleanupPostcode(p).fold("")(_.toString)
}