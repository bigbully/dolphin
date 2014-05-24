package org.dolphin.client

import org.dolphin.client.producer.{AsyncProducer, Producer, SyncProducer}
import akka.actor.{Props, ActorSystem}
import org.dolphin.client.actor.ClientRouterAct
import org.dolphin.client.mail.CreateClient

/**
 * User: bigbully
 * Date: 14-4-26
 * Time: 下午6:08
 */
object ClientFactory {
  private val system = ActorSystem("client")
  private val clientRouterAct = system.actorOf(Props[ClientRouterAct], CLIENT_ROUTER_ACT_NAME)

  def createProducer(conf:ClientConfig):Producer = {
    conf.get(PRODUCER_SEND_MODE) match {
      case ASYNC => {
        val producer = new AsyncProducer(generateId, conf)
        clientRouterAct ! CreateClient(producer)
        producer
      }
      case SYNC => {
        val producer = new SyncProducer(generateId, conf)
        clientRouterAct ! CreateClient(producer)
        producer
      }
    }
  }

}
