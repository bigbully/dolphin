package org.dolphin.broker.actor

import akka.actor.{ActorLogging, Props, ActorRef, Actor}
import org.dolphin.broker._
import org.dolphin.mail.{SendBatchMessage, CreateTopic}
import scala.collection.mutable.ArrayBuffer
import org.dolphin.domain.Message

/**
 * 存储总入口的act
 * User: bigbully
 * Date: 14-5-1
 * Time: 下午10:20
 */
class StoreAct(storeParams: Map[String, String]) extends Actor with ActorLogging{

  import context._

  var topicRouterAct: ActorRef = _
  var walRouterAct: ActorRef = _
  var dataCarrierAct: ActorRef = _

  var startTime:Long = _

  override def receive: Actor.Receive = {
    //接收批量消息
    case SendBatchMessage(array:ArrayBuffer[Message]) => array.foreach(walRouterAct ! _)
    //注册等逻辑
    case Init => {
      startTime = System.currentTimeMillis()
      walRouterAct ! Init
    }
    case InitFinished => {
      log.info("存储模块初始化完成，共耗时{}ms", System.currentTimeMillis() - startTime)
      dataCarrierAct ! Init
    }
    case FindExistentTopics => topicRouterAct ! FindExistentTopics
    case mail@CreateTopic(topicModel) => topicRouterAct ! mail
  }

  @throws[Exception](classOf[Exception])
  override def preStart() {
    topicRouterAct = actorOf(Props(classOf[TopicRouterAct], storeParams), TOPIC_ROUTER_ACT_NAME)
    walRouterAct = actorOf(Props(classOf[WalRouterAct], storeParams), WAL_ROUTER_ACT_NAME)
    dataCarrierAct = actorOf(Props(classOf[DataCarrierAct], storeParams), DATA_CARRIER_ACT_NAME)
  }
}
