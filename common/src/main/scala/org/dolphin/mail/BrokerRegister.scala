package org.dolphin.mail

import org.dolphin.domain.{TopicModel, BrokerModel}

/**
 * User: bigbully
 * Date: 14-5-6
 * Time: 上午12:28
 */
case class BrokerRegister(brokerModel:BrokerModel, topicModels:Option[List[TopicModel]]) {

}
