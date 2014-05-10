package org.dolphin.manager.mail

import org.dolphin.manager.domain.{Topic, Client}

/**
 * User: bigbully
 * Date: 14-5-5
 * Time: 下午11:44
 */
case class TopicFromClient(topic: Topic, client: Client) extends Mail{

}
