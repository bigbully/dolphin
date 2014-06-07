package org.dolphin.mail

import org.dolphin.domain.{TopicModel, BrokerModel}

/**
 * User: bigbully
 * Date: 14-5-7
 * Time: 下午10:15
 */
case class ClientRegisterSuccess(topicModel:TopicModel, brokerList:List[BrokerModel]) {

}
