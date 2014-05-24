package org.dolphin.client.producer

import scala.concurrent.ExecutionContext.Implicits.global
import akka.actor.{ActorRef, Actor}
import org.dolphin.domain.{BrokerModel, TopicModel, ClientModel}
import org.dolphin.common._
import org.dolphin.client._
import akka.pattern._
import org.dolphin.mail.{ClientRegisterSuccess, ClientRegister}
import akka.util.Timeout
import java.util.concurrent.TimeUnit
import org.dolphin.Util.Waiting
import org.slf4j.LoggerFactory
import org.dolphin.client.mail.{RegisterClient, BrokersOnline}

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
    val brokerModelList = enrollAct ? RegisterClient(this, ClientModel(id, PRODUCER), topicModel)

    brokerModelList onSuccess {
      case ClientRegisterSuccess(list) => log.info("注册topic:{}成功，broker列表为{}", topicModel, list)
      case ex: Exception =>
        log.error("注册topic:{}失败，失败信息为:{}", topicModel, ex.getMessage)
        throw ex
    }
    brokerModelList onFailure {
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
