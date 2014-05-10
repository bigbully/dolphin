package org.dolphin.manager.domain

import org.dolphin.domain.ClientModel
import org.dolphin.manager._
import org.dolphin.common._

/**
 * User: bigbully
 * Date: 14-5-5
 * Time: 下午11:52
 */
class Client(val model:ClientModel){
  val id = model.id
  val category = model.category
  val path = ACTOR_ROOT_PATH + "/" + CLIENT_ROUTER_ACT_NAME + "/" + id
  var remotePath:String = _

  override def toString = {
    "id:" + id + ", category:" + (if (category == PRODUCER)"P" else "C")
  }
}
object Client{

  def apply(clientModel:ClientModel) = {
    new Client(clientModel)
  }

  def unapply(client:Client) = {
    Some((client.id, client.category))
  }
}
