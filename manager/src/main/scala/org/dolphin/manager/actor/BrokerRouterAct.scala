package org.dolphin.manager.actor

import akka.actor.{Identify, ActorLogging, Props, Actor}
import org.dolphin.domain.BrokerModel
import org.dolphin.mail.FindLazyBrokers


/**
 * 所有broker的parent
 * User: bigbully
 * Date: 14-4-27
 * Time: 下午9:25
 */
class BrokerRouterAct(val clusterName:String) extends Actor with ActorLogging{
  import context._

  //todo 实现一个查找懒惰的broker的算法
  def findLazyBrokers(topic: String) = {
    List(children.last.path)
  }

  override def receive: Actor.Receive = {
    case brokerModel:BrokerModel => {
      child(brokerModel.id.toString) match {
        case Some(brokerAct) => log.error("已经存在broker:{}, 不继续创建!", brokerModel)
        case None => {
          actorOf(Props(classOf[BrokerAct], brokerModel), brokerModel.id.toString)
          log.info("成功注册了一个broker{}", brokerModel)
        }
      }
    }
    case FindLazyBrokers(topicModel) => {
      val lazyBrokers = findLazyBrokers(topicModel.name)
      sender ! (lazyBrokers, topicModel)
    }
  }
}
