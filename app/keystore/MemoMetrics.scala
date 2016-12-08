package keystore

import address.uk.SelectedAddress
import play.api.libs.json.JsValue
import uk.gov.hmrc.logging.SimpleLogger
import uk.gov.hmrc.play.http.HttpResponse

import scala.concurrent.{ExecutionContext, Future}

// TODO this should be using the normal metricss APIs

class MemoMetrics(peer: MemoService, logger: SimpleLogger, ec: ExecutionContext) extends MemoService {
  private implicit val xec = ec

  override def storeSingleResponse(tag: String, id: String, address: SelectedAddress): Future[HttpResponse] = {
    val now = System.currentTimeMillis
    peer.storeSingleResponse(tag, id, address) map {
      response =>
        val took = System.currentTimeMillis - now
        val uprn = address.normativeAddress.flatMap(_.uprn.map(_.toString)) getOrElse "unknown"
        logger.info(s"Keystore put $tag $id uprn=$uprn took {}ms", took.toString)
        response
    }
  }

  def fetchSingleResponse(tag: String, id: String): Future[Option[JsValue]] = {
    val now = System.currentTimeMillis
    peer.fetchSingleResponse(tag, id) map {
      response =>
        val took = System.currentTimeMillis - now
        logger.info(s"Keystore get $tag $id took {}ms", took.toString)
        response
    }
  }
}
