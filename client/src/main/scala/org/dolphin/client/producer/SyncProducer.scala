package org.dolphin.client.producer

import scala.concurrent.ExecutionContext.Implicits.global
import org.dolphin.domain.{BrokerModel, TopicModel, ClientModel}
import org.dolphin.common._
import org.dolphin.client._
import akka.util.Timeout
import java.util.concurrent.TimeUnit
import org.dolphin.Util.Waiting
import org.slf4j.LoggerFactory
import org.dolphin.client.mail.RegisterClient
import akka.pattern._

/**
 * User: bigbully
 * Date: 14-4-26
 * Time: 下午6:13
 */
class SyncProducer(private[this] val id: String, private[this] val conf: ClientConfig) extends Producer(id, conf) {
  val log = LoggerFactory.getLogger(classOf[SyncProducer])

  def publishAndWaiting(topic: String, cluster: String)(implicit waiting: Waiting) {
    implicit val timeout = Timeout(waiting.seconds, TimeUnit.SECONDS)
    val topicModel = TopicModel(topic, cluster)
    val response = enrollAct ? RegisterClient(this, ClientModel(id, PRODUCER), topicModel)

    response onSuccess {
      case REGISTER_SUCCESS => log.info("注册topic:{}完成", topicModel)
      case ex: Exception =>
        log.error("注册topic:{}失败，失败信息为:{}", topicModel, ex.getMessage)
        throw ex
    }
    response onFailure {
      case ex =>
        log.error("注册topic:{}超时!", topicModel)
        throw ex
    }
  }

  override def publish(topic: String, cluster: String) {
    publishAndWaiting(topic, cluster)(Waiting(3))
  }

  override def send(msg: Array[Byte]) {

  }

  /**
   *
   * @param brokerList
   * @param newBrokers
   * @return
   */
  def freshBrokerList(brokerList: List[BrokerModel], newBrokers: List[BrokerModel]): List[BrokerModel] = {
    brokerList ++: newBrokers
  }


}
