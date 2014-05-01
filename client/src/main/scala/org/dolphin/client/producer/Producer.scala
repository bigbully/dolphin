package org.dolphin.client.producer

import org.dolphin.domain.{ClientModel, TopicModel, BrokerModel}
import akka.actor.{Props, ActorSystem, ActorRef}
import org.dolphin.client.actor.EnrollAct
import org.dolphin.client.ClientConfig
import org.dolphin.common._


/**
 * User: bigbully
 * Date: 14-4-26
 * Time: 下午6:28
 */
abstract class Producer(val id:String, val conf:ClientConfig, val system:ActorSystem) {

  var enrollerAct:ActorRef = _

  def publish(topic:String, cluster:String) {

    val brokerList = register(topic, cluster)
  }

  private def register(topic:String, cluster:String) = {
    enrollerAct = system.actorOf(Props(classOf[EnrollAct], conf.host, conf.port), id)
    val clientModel = ClientModel(id, PRODUCER)
    enrollerAct ! clientModel
    enrollerAct ! TopicModel(topic, cluster, clientModel)
    List(BrokerModel(1, "127.0.0.1", 40000, "myCluster", None))
  }

}
