package org.dolphin.broker.actor

import akka.actor.{Props, ActorLogging, Actor}
import org.dolphin.broker._
import org.dolphin.common._
import org.dolphin.domain.TopicModel
import org.dolphin.broker.store.DataFile
import org.dolphin.broker.mail.DistributeToTopic
import org.dolphin.mail.{TopicCreated, CreateTopic}
import java.io.{RandomAccessFile, File}
import java.util.concurrent.atomic.AtomicInteger

/**
 * User: bigbully
 * Date: 14-5-2
 * Time: 下午1:13
 */
class TopicAct(topicModel: TopicModel, storeParams: Map[String, String]) extends Actor with ActorLogging with FileRouter {

  import context._

  val topicPath = storeParams.get("path") + "/topics/" + topicModel.name
  val enrollAct = actorSelection(ACTOR_ROOT_PATH)
  val metricAct = actorSelection(ACTOR_ROOT_PATH + "/" + METRIC_ACT_NAME)
  var curIndex: String = _
  var curFile: Option[DataFile] = None
  val widget = FileWidget(topicPath, TOPIC_PREFIX, TOPIC_SUFFIX)
  var waitingToBeCheck: AtomicInteger = _
  var file:RandomAccessFile = _

  def saveData(bytes: Array[Byte]) {
    curFile match {
      case None => arrangeCurrentFile
      case Some(myFile) if(myFile.getLength > DEFAULT_MAX_FILE_LENGTH)=> {
        myFile.closeRandomAccessFile(file)
        arrangeCurrentFile
      }
    }

    file.write(MAGIC_HEAD)
    file.writeInt(bytes.length)
    file.write(bytes)
    file.write(MAGIC_TAIL)
  }

  def arrangeCurrentFile {
    val dataFile = createNewFile(children)
    if (dataFile.getLength < DEFAULT_MAX_FILE_LENGTH) {
      dataFile.setLength(DEFAULT_MAX_FILE_LENGTH)
    }
    curFile = Some(dataFile)
    actorOf(Props(classOf[TopicIndexAct], topicModel, storeParams), generateStrId(dataFile.id))
    file = dataFile.openRandomAccessFile
    file.setLength(DEFAULT_MAX_FILE_LENGTH)
  }

  override def receive: Actor.Receive = {
    case DistributeToTopic(subTopic, data) => {
      subTopic match {
        case Some(sub) => //todo 分区
        case None => {
          saveData(data)
        }
      }
    }
    case CheckFile => {
      val files = new File(topicPath).listFiles.filter(file => file.isFile && isFileAvailable(file.getName)).toSeq
      files match {
        case Nil => {
          val newDataFile = createNewFile(children)
          actorOf(Props(classOf[TopicIndexAct], topicModel, storeParams, newDataFile), generateStrId(newDataFile.id))
        }
        case _ => {
          waitingToBeCheck = new AtomicInteger(files.length)
          files.foreach(file => {
            val dataFile = new DataFile(getFileIndex(file), file)
            actorOf(Props(classOf[TopicIndexAct], topicModel, storeParams, dataFile), generateStrId(dataFile.id)) ! CheckFile
          })
        }
      }
    }
    case CheckFileFinished => {
      val remainder = waitingToBeCheck.decrementAndGet()
      if (remainder == 0) {
        parent ! InitFinished
      }
    }
    case CreateTopic(topicModel) => {
      createTopic(topicModel.name)
      metricAct ! TopicCreated(topicModel)
      enrollAct ! TopicCreated(topicModel)
      log.info("创建topic:{}成功!", topicModel.name)
    }
  }

  def createTopic(name: String) {
    val topicDir = new File(storeParams("path") + "/" + name)
    if (!topicDir.exists()) topicDir.mkdir() //创建topic目录


    //todo startTopicJournal
    val subscribeFile = new File(storeParams("path") + "/" + name + "subscriberInfoFile.data")
    if (!subscribeFile.exists()) subscribeFile.createNewFile()
    //todo init subscribe
    //todo 每隔1秒保存subscriberInfoFile.data
  }

}
