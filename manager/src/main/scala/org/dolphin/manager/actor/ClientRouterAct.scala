package org.dolphin.manager.actor

import akka.actor.{Props, Actor}
import org.dolphin.domain.ClientModel
import org.dolphin.common._

/**
 * User: bigbully
 * Date: 14-4-28
 * Time: 下午10:50
 */
class ClientRouterAct extends Actor{
  import context._

  override def receive: Actor.Receive = {
    case model@ClientModel(id, category) => actorOf(Props(classOf[ClientAct], model), id)
  }
}
object ClientRouterAct {
  def getClientPath(clientId: String) = {
    ACTOR_ROOT_PATH + "/" + REGISTRY_ACT_NAME + "/" + CLIENT_ROUTER_ACT_NAME + "/" + clientId
  }
}
