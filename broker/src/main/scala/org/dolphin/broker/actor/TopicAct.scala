package org.dolphin.broker.actor

import akka.actor.{ActorLogging, Actor}
import org.dolphin.broker._
import org.dolphin.domain.TopicModel
import java.io.File
import org.dolphin.mail.{TopicCreated, CreateTopic}

/**
 * User: bigbully
 * Date: 14-5-2
 * Time: 下午1:13
 */
class TopicAct(topicModel: TopicModel, storeParams: Map[String, String]) extends Actor with ActorLogging {

  import context._

  val enrollAct = actorSelection(ACTOR_ROOT_PATH)
  val metricAct = actorSelection(ACTOR_ROOT_PATH + "/" + METRIC_ACT_NAME)

  override def receive: Actor.Receive = {
    case CreateTopic(topicModel) => {
      createTopic(topicModel.name)
      metricAct ! TopicCreated(topicModel)
      enrollAct ! TopicCreated(topicModel)
      log.info("创建topic:{}成功!", topicModel.name)
    }
  }

  def createTopic(name: String) {
    val topicDir = new File(storeParams("path") + "/" + name)
    if (!topicDir.exists()) topicDir.mkdir()//创建topic目录


    //todo startTopicJournal
    val subscribeFile = new File(storeParams("path") + "/" + name + "subscriberInfoFile.data")
    println(subscribeFile.getAbsolutePath)
    if (!subscribeFile.exists()) subscribeFile.createNewFile()
    //todo init subscribe
    //todo 每隔1秒保存subscriberInfoFile.data
  }

}
