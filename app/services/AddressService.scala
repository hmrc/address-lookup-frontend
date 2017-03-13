
package services

import javax.inject.Singleton

import com.google.inject.ImplementedBy
import model.ProposedAddress

import scala.concurrent.Future

@ImplementedBy(classOf[AddressLookupAddressService])
trait AddressService {

  def find(postcode: String, filter: Option[String]): Future[Seq[ProposedAddress]]

}

@Singleton
class AddressLookupAddressService extends AddressService {

  override def find(postcode: String, filter: Option[String]): Future[Seq[ProposedAddress]] = ???

}
