package org.dolphin.client

import akka.actor.ActorRef
import org.dolphin.domain.TopicModel

/**
 * User: bigbully
 * Date: 14-5-24
 * Time: 下午5:32
 */
class Client(private[dolphin] val id: String, private[dolphin] val conf: ClientConfig) {

  var enrollAct:ActorRef = _
  var brokerRouter:ActorRef = _
  var topicModel:TopicModel = _
}
