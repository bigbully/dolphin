package org.dolphin.manager.actor

import akka.actor.{ActorLogging, ActorRef, Props, Actor}
import org.dolphin.domain.{Model, ClientModel, TopicModel, BrokerModel}
import org.dolphin.Util.DolphinException
import org.dolphin.common._
import org.dolphin.mail.ClientRegisterFailure

/**
 * User: bigbully
 * Date: 14-4-28
 * Time: 下午10:00
 */
class ClusterRouterAct extends Actor with ActorLogging {

  import context._

  override def receive: Actor.Receive = {
    case model: BrokerModel => handleCluster(model.cluster, _ ! model, createClusterAndRegister(_, model))
    case model@TopicModel(_, _, from) => handleCluster(model.cluster, _ ! model, cluster => {
      //topic注册分两种情况
      from match {
        case Some(ClientModel(id, _)) => {//如果来自client，必须存在cluster，否则返回异常
          actorSelection(ClientRouterAct.getClientPath(id)) ! ClientRegisterFailure(id, "当前不存在cluster:" + cluster)
          log.error(new DolphinException("当前不存在cluster:" + cluster), "创建topic发生异常!")
        }
        case Some(_) => createClusterAndRegister(cluster, model)//如果来自broker，则创建cluster
      }
    })
  }

  def createClusterAndRegister(clusterName:String, model:Model) {
    actorOf(Props[ClusterAct], clusterName) ! model
  }

  def handleCluster(clusterName: String, existent: ActorRef => Unit, nonExistent: String => Unit) {
    child(clusterName) match {
      case Some(clusterAct) => existent(clusterAct)
      case None => nonExistent(clusterName)
    }
  }


}
