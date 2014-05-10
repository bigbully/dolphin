package org.dolphin.broker.actor

import akka.actor.{Props, ActorRef, Actor}
import org.dolphin.broker._
import org.dolphin.mail.CreateTopic

/**
 * 存储总入口的act
 * User: bigbully
 * Date: 14-5-1
 * Time: 下午10:20
 */
class StoreAct(storeParams: Map[String, String]) extends Actor {

  import context._

  var topicRouterAct: ActorRef = _

  override def receive: Actor.Receive = {
    case FindExistentTopics => topicRouterAct ! FindExistentTopics
    case mail@CreateTopic(topicModel) => topicRouterAct ! mail
  }

  @throws[Exception](classOf[Exception])
  override def preStart() {
    topicRouterAct = actorOf(Props(classOf[TopicRouterAct], storeParams), TOPIC_ROUTER_ACT_NAME)
  }
}
