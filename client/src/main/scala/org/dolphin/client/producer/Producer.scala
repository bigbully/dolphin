package org.dolphin.client.producer

import akka.actor.{ActorSystem, ActorRef}
import org.dolphin.client.ClientConfig


/**
 * User: bigbully
 * Date: 14-4-26
 * Time: 下午6:28
 */
abstract class Producer(val id: String, val conf: ClientConfig, val system: ActorSystem) {

  var enrollerAct: ActorRef = _


}
