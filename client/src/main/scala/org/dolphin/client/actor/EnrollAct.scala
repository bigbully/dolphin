package org.dolphin.client.actor

import akka.actor.{ActorLogging, Actor}
import org.dolphin.common._
import org.dolphin.domain.Model
import org.dolphin.mail.ClientRegisterFailure

/**
 * User: bigbully
 * Date: 14-4-26
 * Time: 下午4:59
 */
class EnrollAct(host: String, port: Int) extends Actor with ActorLogging{
  import context._

  val registryAct = actorSelection("akka.tcp://manager@" + host + ":" + port + "/user/" + REGISTRY_ACT_NAME)


  override def receive: Actor.Receive = {
    case model:Model => registryAct ! model
    case ClientRegisterFailure(clientId, msg) => log.error("client:{}注册失败, 原因为:{}", clientId, msg)
  }
}
