package org.dolphin.manager.domain

import org.dolphin.domain.TopicModel
import org.dolphin.manager._

/**
 * User: bigbully
 * Date: 14-5-5
 * Time: 下午11:56
 */
class Topic(val model:TopicModel){
  val name = model.name
  val cluster = model.cluster

  val path = ACTOR_ROOT_PATH + "/" + CLUSTER_ROUTER_ACT_NAME + "/" + cluster + "/" + TOPIC_ROUTER_ACT_NAME + "/" + name

  override def toString = {
    "name:" + name + ", cluster:" + cluster
  }
}

object Topic {

  def apply(topicModel:TopicModel) = {
    new Topic(topicModel)
  }

  def unapply(topic: Topic) = {
    Some((topic.name, topic.cluster))
  }
}
