package org.dolphin.manager.mail

import org.dolphin.manager.domain.{Broker, Topic}

/**
 * User: bigbully
 * Date: 14-5-6
 * Time: 上午12:49
 */
case class TopicsFromBroker(topics:List[Topic], broker:Broker) extends Mail{

}
