package org.dolphin.manager.actor

import akka.actor.{ActorRef, Props, Actor}
import org.dolphin.domain.{TopicModel, BrokerModel}
import org.dolphin.common._

/**
 * User: bigbully
 * Date: 14-5-1
 * Time: 上午10:12
 */
class ClusterAct(val clusterName:String) extends Actor{

  import context._

  var brokerRouterAct:ActorRef = _
  var topicRouterAct:ActorRef = _

  override def receive: Actor.Receive = {
    case model:BrokerModel => brokerRouterAct ! model
    case model:TopicModel => topicRouterAct ! model
  }

  @throws[Exception](classOf[Exception])
  override def preStart() {
    brokerRouterAct = actorOf(Props(classOf[BrokerRouterAct], clusterName), BROKER_ROUTER_ACT_NAME)
    topicRouterAct = actorOf(Props(classOf[TopicRouterAct], clusterName), TOPIC_ROUTER_ACT_NAME)
  }
}
