package org.dolphin.broker

import akka.actor.{Props, ActorSystem}
import com.typesafe.config.ConfigFactory
import org.dolphin.broker.actor.{StoreAct, EnrollAct}
import org.dolphin.domain.BrokerModel
import org.dolphin.common._

/**
 * User: bigbully
 * Date: 14-4-27
 * Time: 上午9:50
 */
object StartBroker {

  def getExistentTopics = {
    List("topic1", "topic2")
  }

  def main(args:Array[String]) {
    val system = ActorSystem("broker")
    val conf = ConfigFactory.load("broker.conf")
    val id = conf.getInt("broker.id")
    val port = conf.getInt("broker.port")
    val host = if (conf.hasPath("broker.host")) conf.getString("broker.host") else null
    val cluster = if (conf.hasPath("broker.cluster")) conf.getString("broker.cluster") else DEFAULT_CLUSTER
    val managerHost = conf.getString("manager.host")
    val managerPort = conf.getInt("manager.port")
    val storePath = conf.getString("broker.store")
    val enrollAct = system.actorOf(Props(classOf[EnrollAct], managerHost, managerPort), id.toString)
    val storeAct = system.actorOf(Props(classOf[StoreAct], storePath, enrollAct.path), STORE_ACT_NAME)

    val topics = getExistentTopics
    enrollAct ! BrokerModel(id, host, port, cluster, Some(topics))
  }


}

