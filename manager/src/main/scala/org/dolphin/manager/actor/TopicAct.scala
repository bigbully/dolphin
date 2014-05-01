package org.dolphin.manager.actor

import akka.actor.Actor
import org.dolphin.domain.TopicModel

/**
 * User: bigbully
 * Date: 14-4-29
 * Time: 下午6:09
 */
class TopicAct(topicModel:TopicModel) extends Actor{
  override def receive: Actor.Receive = {
    case _ => println("topic anything")
  }
}
