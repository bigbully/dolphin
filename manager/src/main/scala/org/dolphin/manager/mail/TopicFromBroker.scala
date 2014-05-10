package org.dolphin.manager.mail

import org.dolphin.manager.domain.{Topic, Broker}

/**
 * User: bigbully
 * Date: 14-5-6
 * Time: 上午1:39
 */
case class TopicFromBroker(topic:Topic, broker:Broker) extends Mail{

}
