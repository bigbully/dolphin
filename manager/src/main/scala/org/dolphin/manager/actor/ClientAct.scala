package org.dolphin.manager.actor

import akka.actor.Actor
import org.dolphin.mail.ClientRegisterFailure
import org.dolphin.manager.domain.Client

/**
 * User: bigbully
 * Date: 14-4-29
 * Time: 下午9:01
 */
class ClientAct(val client: Client) extends Actor {

  import context._

  val from = actorSelection(client.remotePath)

  override def receive: Actor.Receive = {
    case mail@_ => from ! mail

  }
}
