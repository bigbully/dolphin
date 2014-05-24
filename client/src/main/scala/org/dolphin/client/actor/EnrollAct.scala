package org.dolphin.client.actor

import akka.actor.{Props, ActorRef, ActorLogging, Actor}
import org.dolphin.common._
import org.dolphin.domain.{BrokerModel, ClientModel, TopicModel, Model}
import org.dolphin.mail.{SendBatchMessage, ClientRegisterSuccess, ClientRegister, ClientRegisterFailure}
import org.dolphin.client._
import org.dolphin.client.mail.{RegisterClient, BrokersOnline, BrokersOnlineFinished}
import org.dolphin.client.producer.{AsyncProducer, SyncProducer}

/**
 * User: bigbully
 * Date: 14-4-26
 * Time: 下午4:59
 */
class EnrollAct(host: String, port: Int) extends Actor with ActorLogging{
  import context._

  val registryActPath = "akka.tcp://manager@" + host + ":" + port + "/user/" + REGISTRY_ACT_NAME
  var syncProducerActRef :ActorRef = _
  var brokerRouterAct :ActorRef = _
  var client:Client = _

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
    case mail:ClientRegisterSuccess => {
      client match {
        case _:SyncProducer => syncProducerActRef ! mail
        case producer:AsyncProducer => {
          log.info("注册topic成功! broker列表为{}", mail.brokerList)
          producer.startSendThread
        }
      }
    }
    //向broker发送消息
    case mail:SendBatchMessage => {

    }
  }

  def registryAct = actorSelection(registryActPath)

  @throws[Exception](classOf[Exception])
  override def preStart(){
    brokerRouterAct = actorOf(Props[BrokerRouterAct], BROKER_ROUTER_ACT_NAME)
  }
}
