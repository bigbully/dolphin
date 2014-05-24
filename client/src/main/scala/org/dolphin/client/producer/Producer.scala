package org.dolphin.client.producer

import akka.actor.{Actor, ActorSystem, ActorRef}
import org.dolphin.client.{Client, ClientConfig}


/**
 * User: bigbully
 * Date: 14-4-26
 * Time: 下午6:28
 */
abstract class Producer(private val id: String, private val conf: ClientConfig) extends Client(id, conf){

  def publish(topic:String, cluster:String)

  def send(msg:Array[Byte])

}
