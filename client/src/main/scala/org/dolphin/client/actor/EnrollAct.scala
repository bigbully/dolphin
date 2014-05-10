package org.dolphin.client.actor

import akka.actor.{Props, ActorRef, ActorLogging, Actor}
import org.dolphin.common._
import org.dolphin.domain.{BrokerModel, ClientModel, TopicModel, Model}
import org.dolphin.mail.{ClientRegisterSuccess, ClientRegister, ClientRegisterFailure}
import org.dolphin.client._
import org.dolphin.client.mail.BrokersOnlineFinished

/**
 * User: bigbully
 * Date: 14-4-26
 * Time: 下午4:59
 */
class EnrollAct(host: String, port: Int) extends Actor with ActorLogging{
  import context._

  val registryActPath = "akka.tcp://manager@" + host + ":" + port + "/user/" + REGISTRY_ACT_NAME
  var syncProducer :ActorRef = _
  var brokerRouterAct :ActorRef = _

  override def receive: Actor.Receive = {
    //与manager通信
    case clientRegister:ClientRegister => {
      syncProducer = sender
      registryAct ! clientRegister
    }
    //接受manager的指令
    case ClientRegisterFailure(clientId, msg) => syncProducer ! new RuntimeException(msg)
    case mail:ClientRegisterSuccess => syncProducer ! mail
    case BrokersOnlineFinished(brokerIds) => log.info("broker:{}上线成功!", brokerIds)
  }

  def registryAct = {
    actorSelection(registryActPath)
  }

  @throws[Exception](classOf[Exception])
  override def preStart(){
    brokerRouterAct = actorOf(Props[BrokerRouterAct], BROKER_ROUTER_ACT_NAME)
  }
}
