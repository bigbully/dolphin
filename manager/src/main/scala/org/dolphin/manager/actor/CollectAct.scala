package org.dolphin.manager.actor

import akka.actor.Actor
import org.dolphin.common._


/**
 * User: bigbully
 * Date: 14-4-29
 * Time: 下午7:38
 */
class CollectAct extends Actor{
  override def receive: Actor.Receive = {
    case COLLECT_MAIL => println("my job")
  }
}
