package org.dolphin.broker.actor

import akka.actor.{ActorLogging, Actor}
import org.dolphin.domain.{TopicHeartBeat, TopicModel}
import org.dolphin.mail.TopicCreated

/**
 * 度量用的act
 * User: bigbully
 * Date: 14-5-2
 * Time: 下午1:36
 */
class MetricAct extends Actor with ActorLogging{

  var topicInfoMap = Map.empty[String, TopicHeartBeat]

  override def receive: Actor.Receive = {
    case TopicCreated(TopicModel(topicName, _), _) => {
      log.info("metric接收到新的topic信息:{}", topicName)
      topicInfoMap += (topicName -> new TopicHeartBeat(topicName))
    }
  }


}


