package org.dolphin.client

import org.dolphin.client.producer.SyncProducer
import akka.actor.ActorSystem

/**
 * User: bigbully
 * Date: 14-4-26
 * Time: 下午6:08
 */
object ClientFactory {
  private val system = ActorSystem("client")

  def createProducer(conf:ClientConfig) = {
    new SyncProducer(generateId, conf, system)
  }

}
