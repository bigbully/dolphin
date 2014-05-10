package org.dolphin.manager.actor

import akka.actor.{ActorLogging, ActorRef, Props, Actor}
import org.dolphin.domain.{Model, ClientModel, TopicModel, BrokerModel}
import org.dolphin.Util.DolphinException
import org.dolphin.common._
import org.dolphin.mail.{ClientRegister, ClientRegisterFailure}
import org.dolphin.manager.mail.{Mail, TopicsFromBroker, RegisterBroker, TopicFromClient}
import org.dolphin.manager.domain.Broker

/**
 * User: bigbully
 * Date: 14-4-28
 * Time: 下午10:00
 */
class ClusterRouterAct extends Actor with ActorLogging {

  import context._

  override def receive: Actor.Receive = {
    case mail@TopicFromClient(topic, client) => handleCluster(topic.cluster, _ ! mail, cluster => {
      actorSelection(client.path) ! ClientRegisterFailure(client.id, "当前不存在cluster:" + cluster)
      log.error(new DolphinException("当前不存在cluster:" + cluster), "创建topic发生异常!")
    })
    case mail@RegisterBroker(broker) => handleCluster(broker.cluster, _ ! mail, createAndSendMail(_, mail))
    case mail@TopicsFromBroker(topics, broker) => handleCluster(broker.cluster, _ ! mail, createAndSendMail(_, mail))
  }

  def handleCluster(clusterName: String, existent: ActorRef => Unit, nonExistent: String => Unit) {
    child(clusterName) match {
      case Some(clusterAct) => existent(clusterAct)
      case None => nonExistent(clusterName)
    }
  }

  def createAndSendMail(cluster:String, mail:Mail){
    actorOf(Props(classOf[ClusterAct], cluster), cluster) ! mail
  }

}
