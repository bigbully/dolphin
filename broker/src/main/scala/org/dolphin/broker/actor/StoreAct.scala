package org.dolphin.broker.actor

import akka.actor.{ActorLogging, Props, ActorRef, Actor}
import org.dolphin.broker._
import org.dolphin.mail.CreateTopic

/**
 * 存储总入口的act
 * User: bigbully
 * Date: 14-5-1
 * Time: 下午10:20
 */
class StoreAct(storeParams: Map[String, String]) extends Actor with ActorLogging{

  import context._

  var topicRouterAct: ActorRef = _
  var walAct: ActorRef = _

  var startTime:Long = _

  override def receive: Actor.Receive = {
    case Init => {
      startTime = System.currentTimeMillis()
      walAct ! Init
    }
    case InitFinished => {
      log.info("存储模块初始化完成，共耗时{}ms", System.currentTimeMillis() - startTime)
    }
    case FindExistentTopics => topicRouterAct ! FindExistentTopics
    case mail@CreateTopic(topicModel) => topicRouterAct ! mail
  }

  @throws[Exception](classOf[Exception])
  override def preStart() {
    topicRouterAct = actorOf(Props(classOf[TopicRouterAct], storeParams), TOPIC_ROUTER_ACT_NAME)
    walAct = actorOf(Props(classOf[WalRouterAct], storeParams), WAL_ACT_NAME)
  }
}
