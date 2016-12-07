package keystore

import address.uk.AddressRecordWithEdits
import uk.gov.hmrc.address.v2.International
import uk.gov.hmrc.logging.SimpleLogger
import uk.gov.hmrc.play.http.HttpResponse

import scala.concurrent.{ExecutionContext, Future}

// TODO this should be using the normal metricss APIs

class MemoMetrics(peer: MemoService, logger: SimpleLogger, ec: ExecutionContext) extends MemoService {
  private implicit val xec = ec

  override def fetchSingleUkResponse(tag: String, id: String): Future[Option[AddressRecordWithEdits]] = {
    val now = System.currentTimeMillis
    peer.fetchSingleUkResponse(tag, id) map {
      response =>
        val took = System.currentTimeMillis - now
        logger.info(s"Keystore get $tag $id took {}ms", took.toString)
        response
    }
  }

  override def fetchSingleIntResponse(tag: String, id: String): Future[Option[International]] = {
    val now = System.currentTimeMillis
    peer.fetchSingleIntResponse(tag, id) map {
      response =>
        val took = System.currentTimeMillis - now
        logger.info(s"Keystore get $tag $id took {}ms", took.toString)
        response
    }
  }

  override def storeSingleUkResponse(tag: String, id: String, address: AddressRecordWithEdits): Future[HttpResponse] = {
    val now = System.currentTimeMillis
    peer.storeSingleUkResponse(tag, id, address) map {
      response =>
        val took = System.currentTimeMillis - now
        val uprn = address.normativeAddress.flatMap(_.uprn.map(_.toString)) getOrElse "unknown"
        logger.info(s"Keystore put $tag $id uprn=$uprn took {}ms", took.toString)
        response
    }
  }

  override def storeSingleIntResponse(tag: String, id: String, address: International): Future[HttpResponse] = {
    val now = System.currentTimeMillis
    peer.storeSingleIntResponse(tag, id, address) map {
      response =>
        val took = System.currentTimeMillis - now
        logger.info(s"Keystore put $tag $id took {}ms", took.toString)
        response
    }
  }
}
