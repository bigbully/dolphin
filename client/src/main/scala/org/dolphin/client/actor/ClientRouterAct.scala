package org.dolphin.client.actor

import akka.actor.{Props, Actor}
import org.dolphin.client.mail.CreateClient

/**
 * User: bigbully
 * Date: 14-5-24
 * Time: 下午5:07
 */
class ClientRouterAct extends Actor{
  import context._

  override def receive: Actor.Receive = {
    case CreateClient(client) => {
      client.enrollAct = actorOf(Props(classOf[EnrollAct], client.conf), client.id)
    }
  }
}
