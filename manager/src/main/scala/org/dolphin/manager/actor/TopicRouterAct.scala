package org.dolphin.manager.actor

import akka.actor.{ActorPath, ActorLogging, Props, Actor}
import org.dolphin.domain.{ClientModel, Model, TopicModel}
import org.dolphin.mail.{ClientRegisterFailure, FindLazyBrokers}
import org.dolphin.common._
import org.dolphin.Util.DolphinException

/**
 * User: bigbully
 * Date: 14-4-28
 * Time: 下午10:54
 */
class TopicRouterAct(val clusterName:String) extends Actor with ActorLogging{
  import context._

  override def receive: Actor.Receive = {
    case topicModel:TopicModel => {
      child(topicModel.name) match {
        case Some(brokerAct) => {

        }
        case None => {
          getBrokerRouterActRef ! FindLazyBrokers(topicModel)
          actorOf(Props(classOf[TopicAct], topicModel), topicModel.name)
          log.info("注册topic:{}成功", topicModel.name)
        }
      }
    }
    case (lazyBrokers:List[ActorPath], topicModel@TopicModel(name, _, Some(from))) => {
      lazyBrokers match {
        case Nil => {
          val clientId = from.asInstanceOf[ClientModel].id
          val ex = new DolphinException("所有broker处于繁忙状态，无法创建topic!")
          actorSelection(ClientRouterAct.getClientPath(clientId)) ! ClientRegisterFailure(clientId, ex.getMessage)
          log.error(ex, "创建topic失败!")
        }
        case _ => {
          lazyBrokers.foreach(actorSelection(_) ! topicModel)
        }
      }

    }
  }

  def getBrokerRouterActRef = {
    actorSelection(parent.path.child(BROKER_ROUTER_ACT_NAME))
  }
}
