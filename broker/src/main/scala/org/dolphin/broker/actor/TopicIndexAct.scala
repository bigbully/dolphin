package org.dolphin.broker.actor

import akka.actor.{ActorLogging, Actor}
import org.dolphin.domain.TopicModel
import org.dolphin.broker.store.{DataByteArrayInputStream, DataFileAccessor, DataFile}
import org.dolphin.broker._
import java.io.IOException
import scala.annotation.tailrec

/**
 * User: bigbully
 * Date: 14-6-11
 * Time: 上午12:12
 */
class TopicIndexAct(topicModel: TopicModel, storeParams: Map[String, String], dataFile: DataFile) extends Actor with ActorLogging {

  import context._

  override def receive: Actor.Receive = {
    case CheckFile => {
      recoveryCheck(dataFile)
      parent ! CheckFileFinished
    }
  }

  def recoveryCheck(file: DataFile) {
    val reader = topicAccessorPool.openDataFileAccessor(file)
    val offset = try {
      recursiveCheck(reader, 0)
    } finally {
      walAccessorPool.closeDataFileAccessor(reader)
    }
    file.setLength(offset)
  }

  @tailrec private def recursiveCheck(reader: DataFileAccessor, offset: Int): Int = {
    val size = try {
      val mySize = checkTopicFile(reader, offset)
      if (mySize <= 0){
        truncate(reader, offset)
        return offset
      }else {
        mySize
      }
    } catch {
      case e: IOException => return offset //当读完文件时退出
    }
    recursiveCheck(reader, offset + MAGIC_HEAD_LENGTH + size + MAGIC_TAIL_LENGTH)
  }

  def truncate(reader: DataFileAccessor, offset: Int) {
    reader.getFile.setLength(offset)
  }

  def checkTopicFile(reader: DataFileAccessor, offset: Int): Int = {
    //验证头
    val head = new Array[Byte](MAGIC_HEAD_LENGTH)
    reader.readFully(offset, head)
    val headStream = new DataByteArrayInputStream(head)
    (0 until MAGIC_HEAD_LENGTH).foreach(i => if (headStream.readByte != MAGIC_HEAD(i)) return -1)
    val size = headStream.readInt
    if (size > DEFAULT_MAX_WRITE_BATCH_SIZE) return -1

    //验证尾
    val tail = new Array[Byte](MAGIC_TAIL_LENGTH)
    reader.readFully(offset + BATCH_CONTROL_RECORD_SIZE + size, tail)
    val tailStream = new DataByteArrayInputStream(tail)
    (0 until MAGIC_TAIL_LENGTH).foreach(i => if (tailStream.readByte != MAGIC_TAIL(i)) return -1)

    size
  }
}
