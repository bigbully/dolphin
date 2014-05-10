package org.dolphin.client.producer

import scala.concurrent.ExecutionContext.Implicits.global
import akka.actor.{Props, ActorSystem}
import org.dolphin.client.actor.EnrollAct
import org.dolphin.domain.{BrokerModel, TopicModel, ClientModel}
import org.dolphin.common._
import org.dolphin.client._
import akka.pattern._
import org.dolphin.mail.{ClientRegisterSuccess, ClientRegister}
import akka.util.Timeout
import java.util.concurrent.TimeUnit
import org.dolphin.Util.Waiting
import org.slf4j.LoggerFactory
import org.dolphin.client.mail.BrokersOnline

/**
 * User: bigbully
 * Date: 14-4-26
 * Time: 下午6:13
 */
class SyncProducer(private[this] val id: String, private[this] val conf: ClientConfig, private[this] val system: ActorSystem) extends Producer(id, conf, system) {
  val log = LoggerFactory.getLogger(classOf[SyncProducer])

  def publish(topic: String, cluster: String)(implicit waiting: Waiting) {
    enrollerAct = system.actorOf(Props(classOf[EnrollAct], conf.host, conf.port), id)
    implicit val timeout = Timeout(waiting.seconds, TimeUnit.SECONDS)
    val topicModel = TopicModel(topic, cluster)
    val brokerModelList = enrollerAct ? ClientRegister(ClientModel(id, PRODUCER), topicModel)

    brokerModelList onSuccess {
      case ClientRegisterSuccess(list) => {
        brokerRouterAct ! BrokersOnline(list)
      }
      case ex: Exception =>
        log.error("发布topic:{}失败，失败信息为:{}", topicModel, ex.getMessage)
        throw ex
    }
    brokerModelList onFailure {
      case ex =>
        log.error("发布topic:{}超时!", topicModel)
        throw ex
    }
  }

  def brokerRouterAct = {
    system.actorSelection(ACTOR_ROOT_PATH + "/" + BROKER_ROUTER_ACT_NAME)
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
