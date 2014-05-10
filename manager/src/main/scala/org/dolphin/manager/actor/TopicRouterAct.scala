package org.dolphin.manager.actor

import akka.actor.{ActorPath, ActorLogging, Props, Actor}
import org.dolphin.domain.{ClientModel, TopicModel}
import org.dolphin.mail.ClientRegisterFailure
import org.dolphin.common._
import org.dolphin.Util.DolphinException
import org.dolphin.manager.mail._
import org.dolphin.manager.domain.Topic
import scala.Some
import org.dolphin.manager.mail.TopicFromClient
import org.dolphin.manager.mail.TopicsFromBroker
import org.dolphin.manager.domain.Topic

/**
 * User: bigbully
 * Date: 14-4-28
 * Time: 下午10:54
 */
class TopicRouterAct(val cluster: String) extends Actor with ActorLogging {

  import context._

  override def receive: Actor.Receive = {
    case mail@TopicFromClient(topic, client) => handleTopic(topic, mail)
    case mail@TopicsFromBroker(topics, broker) => topics.foreach(topic => handleTopic(topic, TopicFromBroker(topic, broker)))
  }

  def handleTopic(topic:Topic, mail:Mail) {
    child(topic.name) match {
      case Some(topicAct) => topicAct ! mail
      case None => {
        actorOf(Props(classOf[TopicAct], topic), topic.name) ! mail
        log.info("注册topic:{}成功", topic.name)
      }
    }
  }
}
