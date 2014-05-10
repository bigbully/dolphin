package org.dolphin.manager.mail

import akka.actor.ActorPath

/**
 * User: bigbully
 * Date: 14-5-6
 * Time: 上午1:26
 */
case class LazyBrokersForTopic(brokers: List[ActorPath]) {

}
