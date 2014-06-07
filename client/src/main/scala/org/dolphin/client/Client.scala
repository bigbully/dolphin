package org.dolphin.client

import akka.actor.ActorRef

/**
 * User: bigbully
 * Date: 14-5-24
 * Time: 下午5:32
 */
class Client(val id: String, val conf: ClientConfig) {

  var enrollAct:ActorRef = _
  var brokerRouter:ActorRef = _
  var topic:String = _
}
