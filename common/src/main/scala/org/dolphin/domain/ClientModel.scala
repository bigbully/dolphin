package org.dolphin.domain

/**
 * User: bigbully
 * Date: 14-4-28
 * Time: 下午10:44
 */
case class ClientModel(id:String, category:Int) extends Model{
  var path:String = _

  def this(clientModel:ClientModel, path:String) = {
    this(clientModel.id, clientModel.category)
    this.path = path
  }
}
object ClientModel{
  def apply(clientModel:ClientModel, path:String) = {
    new ClientModel(clientModel, path)
  }
}
