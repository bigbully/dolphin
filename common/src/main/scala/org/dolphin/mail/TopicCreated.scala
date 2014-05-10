package org.dolphin.mail

import org.dolphin.domain.{BrokerModel, TopicModel}

/**
 * User: bigbully
 * Date: 14-5-6
 * Time: 上午1:50
 */
case class TopicCreated(topicModel:TopicModel, brokerModel:BrokerModel) {

  def this(topicModel:TopicModel) = {
    this(topicModel, null)
  }
}
object TopicCreated {
  def apply(topicModel:TopicModel) = {
    new TopicCreated(topicModel)
  }

}
