package org.dolphin.manager.actor

import akka.actor.Actor
import org.dolphin.domain.{TopicModel, BrokerModel}


/**
 * User: bigbully
 * Date: 14-4-28
 * Time: 下午9:58
 */
class BrokerAct(brokerModel:BrokerModel) extends Actor{
  import context._

  var topics = Set.empty[String]//当前broker所带的topic

  override def receive: Actor.Receive = {
    case TopicModel(name, cluster, _) => {//注册topic
      actorSelection(brokerModel.path) ! TopicModel(name, cluster, None)
      topics += name
    }
  }
}
