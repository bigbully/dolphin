package org.dolphin.broker.actor

import akka.actor.{ActorPath, Actor}
import org.dolphin.domain.TopicModel
import org.dolphin.mail.TopicCreated

/**
 * User: bigbully
 * Date: 14-5-1
 * Time: 下午10:20
 */
class StoreAct(storePath:String, enrollActPath:ActorPath) extends Actor{
  import context._


  def addTopic(topic: String) {

  }

  override def receive: Actor.Receive = {
    case TopicModel(name, _, _) => addTopic(name)
    case _:TopicCreated => actorSelection(enrollActPath) ! _
  }
}
