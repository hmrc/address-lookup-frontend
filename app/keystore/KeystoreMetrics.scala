package keystore

import address.uk.AddressRecordWithEdits
import uk.gov.hmrc.logging.SimpleLogger
import uk.gov.hmrc.play.http.HttpResponse

import scala.concurrent.{ExecutionContext, Future}

class KeystoreMetrics(peer: KeystoreService, logger: SimpleLogger, ec: ExecutionContext) extends KeystoreService {
  private implicit val xec = ec

  override def fetchSingleResponse(id: String, tag: String): Future[Option[AddressRecordWithEdits]] = {
    val now = System.currentTimeMillis
    peer.fetchSingleResponse(id, tag) map {
      response =>
        val took = System.currentTimeMillis - now
        logger.info(s"Keystore get $id $tag took {}ms", took.toString)
        response
    }
  }

  override def storeSingleResponse(id: String, tag: String, address: AddressRecordWithEdits): Future[HttpResponse] = {
    val now = System.currentTimeMillis
    peer.storeSingleResponse(id, tag, address) map {
      response =>
        val took = System.currentTimeMillis - now
        val uprn = address.normativeAddress.flatMap(_.uprn.map(_.toString)) getOrElse "unknown"
        logger.info(s"Keystore put $id $tag uprn=$uprn took {}ms", took.toString)
        response
    }
  }
}
