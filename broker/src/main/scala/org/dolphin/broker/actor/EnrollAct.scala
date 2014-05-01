package org.dolphin.broker.actor

import akka.actor.Actor
import org.dolphin.domain.{TopicModel, BrokerModel}
import org.dolphin.common._


/**
 * User: bigbully
 * Date: 14-4-26
 * Time: 下午4:59
 */
class EnrollAct(host: String, port: Int) extends Actor {
  import context._

  val registryAct = actorSelection("akka.tcp://manager@" + host + ":" + port + "/user/" + REGISTRY_ACT_NAME)
  val storeAct = actorSelection(ACTOR_ROOT_PATH + "/" + STORE_ACT_NAME)

  override def receive: Actor.Receive = {
    case brokerModel: BrokerModel => registryAct ! brokerModel
    case _:TopicModel => storeAct ! _
  }

}
