package org.dolphin.manager.actor

import akka.actor.Actor
import org.dolphin.domain.ClientModel
import org.dolphin.mail.ClientRegisterFailure

/**
 * User: bigbully
 * Date: 14-4-29
 * Time: 下午9:01
 */
class ClientAct(val clientModel:ClientModel) extends Actor{
  import context._

  val from = actorSelection(clientModel.path)

  override def receive: Actor.Receive = {
    case failure@ClientRegisterFailure => from ! failure
  }
}
