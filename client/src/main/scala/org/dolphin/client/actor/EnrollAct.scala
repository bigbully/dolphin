package org.dolphin.client.actor

import akka.actor.{Props, ActorRef, ActorLogging, Actor}
import org.dolphin.common._
import org.dolphin.domain.{BrokerModel, ClientModel, TopicModel, Model}
import org.dolphin.mail.{SendBatchMessage, ClientRegisterSuccess, ClientRegister, ClientRegisterFailure}
import org.dolphin.client._
import org.dolphin.client.mail.{BrokerRouterCreated, BrokersOnline, RegisterClient, BrokersOnlineFinished}
import org.dolphin.client.producer.{AsyncProducer, SyncProducer}
import akka.routing.{BroadcastGroup, ActorRefRoutee, Router, RoundRobinRoutingLogic}

/**
 * User: bigbully
 * Date: 14-4-26
 * Time: 下午4:59
 */
class EnrollAct(conf:ClientConfig) extends Actor with ActorLogging{

  import context._
  val host = conf.host
  val port = conf.port
  val registryActPath = "akka.tcp://manager@" + host + ":" + port + "/user/" + REGISTRY_ACT_NAME
  var syncProducerActRef :ActorRef = _
  var brokerGroupAct :ActorRef = _
  var client:Client = _
  var brokerReceiver:ActorRef = _

  override def receive: Actor.Receive = {
    //与manager通信
    case mail:RegisterClient => {
      syncProducerActRef = sender
      client = mail.client
      registryAct ! ClientRegister(mail.clientModel, mail.topicModel)
    }
    //接受manager的指令
    case ClientRegisterFailure(clientId, msg) => {
      client match {
        case _:SyncProducer => syncProducerActRef ! new RuntimeException(msg)
        case _:AsyncProducer => log.error("注册topic失败! 失败信息为:{}", msg)
      }
    }
    case ClientRegisterSuccess(topicModel, brokerList) => {
      log.info("topic:{}所在的broker列表为{}", topicModel, brokerList)
      brokerGroupAct ! BrokersOnline(brokerList)
      client match {
        case _:SyncProducer => syncProducerActRef ! REGISTER_SUCCESS
        case producer:AsyncProducer => producer.startSendThread
      }
    }
    case BrokerRouterCreated(router) => client.brokerRouter = router
  }

  def registryAct = actorSelection(registryActPath)

  @throws[Exception](classOf[Exception])
  override def preStart(){
    brokerGroupAct = actorOf(Props(classOf[BrokerGroupAct], conf), BROKER_GROUP_ACT_NAME)
  }
}
