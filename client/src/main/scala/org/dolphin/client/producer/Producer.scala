package org.dolphin.client.producer

import akka.actor.{Actor, ActorSystem, ActorRef}
import org.dolphin.client.{Client, ClientConfig}
import org.dolphin.domain.BrokerModel


/**
 * User: bigbully
 * Date: 14-4-26
 * Time: 下午6:28
 */
abstract class Producer(id: String, conf: ClientConfig) extends Client(id, conf){

  def publish(topic:String, cluster:String)

  def send(msg:Array[Byte])

}
