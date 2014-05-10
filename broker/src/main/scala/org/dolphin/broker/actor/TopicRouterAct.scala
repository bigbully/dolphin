package org.dolphin.broker.actor

import akka.actor.{ActorLogging, Props, Actor}
import org.dolphin.common._
import org.dolphin.domain.TopicModel
import org.dolphin.mail.{TopicCreated, CreateTopic}
import org.dolphin.broker._
import scala.Some
import org.dolphin.mail.CreateTopic
import org.dolphin.broker.mail.ExistentTopics

/**
 * User: bigbully
 * Date: 14-5-2
 * Time: 下午1:04
 */
class TopicRouterAct(storeParams:Map[String,String]) extends Actor with ActorLogging{
  import context._

  val cluster = storeParams("cluster")
  val enrollAct = actorSelection(ACTOR_ROOT_PATH)

  override def receive: Actor.Receive = {
    case mail@CreateTopic(topicModel)=> {
      child(topicModel.name) match {
        case None => actorOf(Props(classOf[TopicAct], topicModel, storeParams), topicModel.name) ! mail
        case Some(_) => sender ! mail
      }
    }
    case FindExistentTopics => {
      val topics = findExistentTopics
      topics match {
        case None => log.info("没找到任何现存的topic!")
        case Some(topics) => {
          log.info("找到现存的topic为:{}", topics)
          topics.foreach(topicModel => actorOf(Props(classOf[TopicAct], topicModel), topicModel.name))
        }
      }
      enrollAct ! ExistentTopics(topics)
    }
  }

  //todo
  def findExistentTopics:Option[List[TopicModel]] = {
    None
  }

}
