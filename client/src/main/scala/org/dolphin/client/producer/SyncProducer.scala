package org.dolphin.client.producer

import org.dolphin.client.ClientConfig
import akka.actor.ActorSystem

/**
 * User: bigbully
 * Date: 14-4-26
 * Time: 下午6:13
 */
class SyncProducer(private[this] val id:String, private[this] val conf:ClientConfig, private[this] val system:ActorSystem) extends Producer(id, conf, system){

}
