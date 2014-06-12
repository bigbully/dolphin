package org.dolphin.broker.actor

import akka.actor.{ActorLogging, Props, Actor}
import org.dolphin.common._
import org.dolphin.domain.TopicModel
import org.dolphin.mail.{TopicCreated, CreateTopic}
import org.dolphin.broker._
import scala.Some
import org.dolphin.mail.CreateTopic
import org.dolphin.broker.mail.ExistentTopics
import java.io.File
import java.util.concurrent.atomic.AtomicInteger

/**
 * User: bigbully
 * Date: 14-5-2
 * Time: 下午1:04
 */
class TopicRouterAct(storeParams:Map[String,String]) extends Actor with ActorLogging{
  import context._

  val path = storeParams("path") + "/topics"
  val cluster = storeParams("cluster")
  val enrollAct = actorSelection(ACTOR_ROOT_PATH)
  var waitingToBeCheck:AtomicInteger = _

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
        case None => {
          log.info("没找到任何现存的topic!")
          enrollAct ! ExistentTopics(None)
        }
        case Some(topics) => {
          log.info("找到现存的topic为:{}", topics)
          waitingToBeCheck = new AtomicInteger(topics.size)
          topics.foreach(topicModel => actorOf(Props(classOf[TopicAct], topicModel, storeParams), topicModel.name) ! CheckFile)
        }
      }

    }
    case InitFinished => {
      val remainder = waitingToBeCheck.decrementAndGet()
      if (remainder == 0) {
        enrollAct ! ExistentTopics(Some(children.toList.map(actorRef => TopicModel(actorRef.path.name, cluster))))
      }
    }
  }

  def findExistentTopics:Option[List[TopicModel]] = {
    val topicRouterDir = new File(path)
    if (!topicRouterDir.exists()){//创建topicRouter目录
      topicRouterDir.mkdir()
      None
    }else {
      Some(topicRouterDir.listFiles().map(file => TopicModel(file.getName, cluster)).toList)
    }
  }

}
