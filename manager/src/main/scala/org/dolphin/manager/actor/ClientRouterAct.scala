package org.dolphin.manager.actor

import akka.actor.{Props, Actor}
import org.dolphin.domain.ClientModel
import org.dolphin.common._
import org.dolphin.manager.mail.RegisterClient

/**
 * User: bigbully
 * Date: 14-4-28
 * Time: 下午10:50
 */
class ClientRouterAct extends Actor{
  import context._

  override def receive: Actor.Receive = {
    case RegisterClient(client) => actorOf(Props(classOf[ClientAct], client), client.id)
  }
}

