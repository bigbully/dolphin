package org.dolphin.client.actor

import akka.actor.Actor
import org.dolphin.domain.BrokerModel

/**
 * User: bigbully
 * Date: 14-5-10
 * Time: 下午9:24
 */
class BrokerAct(val brokerModel:BrokerModel) extends Actor{
  override def receive: Actor.Receive = {
    case _ =>
  }
}
