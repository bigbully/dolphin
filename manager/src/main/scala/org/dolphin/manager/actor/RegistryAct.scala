package org.dolphin.manager.actor

import akka.actor.{Props, ActorRef, Actor}
import org.dolphin.domain.{TopicModel, ClientModel, Model, BrokerModel}
import org.dolphin.common._


/**
 * User: bigbully
 * Date: 14-4-26
 * Time: 下午5:03
 */
class RegistryAct extends Actor {

  import context._

  var clusterRouterAct: ActorRef = _
  var clientRouterAct: ActorRef = _

  /**
   * 根据brokerModel的不同情况进行不同的注册步骤
   * @param brokerModel
   */
  def prepare(brokerModel: BrokerModel) {
    brokerModel match {
      case BrokerModel(id, null, port, cluster, topics) => prepare(BrokerModel(id, sender.path.address.host.get, port, cluster, topics))
      case BrokerModel(_, _, _, cluster, Some(topics)) => {
        topics.foreach(topic => clusterRouterAct ! TopicModel(topic, cluster, Some(brokerModel)))
        clusterRouterAct ! brokerModel
      }
      case _ => clusterRouterAct ! brokerModel
    }
  }
  
  override def receive: Actor.Receive = {
    case model: ClientModel => clientRouterAct ! ClientModel(model, sender.path.toString)
    case model: TopicModel => clusterRouterAct ! model
    case model: BrokerModel => println(sender.path);prepare(model)
  }

  @throws[Exception](classOf[Exception])
  override def preStart() {
    clusterRouterAct = actorOf(Props(classOf[ClusterRouterAct]), CLUSTER_ROUTER_ACT_NAME)
    clientRouterAct = actorOf(Props(classOf[ClientRouterAct]), CLIENT_ROUTER_ACT_NAME)
  }

}

