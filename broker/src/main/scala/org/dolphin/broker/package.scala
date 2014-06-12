package org.dolphin

import java.io.{File, IOException, UnsupportedEncodingException}
import org.dolphin.Util.DolphinException
import org.dolphin.broker.store.{DataFile, DataFileAccessorPool, DataByteArrayOutputStream}
import scala.collection.immutable
import akka.actor.ActorRef

/**
 * User: bigbully
 * Date: 14-5-6
 * Time: 上午12:18
 */
package object broker {
  val REGISTER = "register"

  val METRIC_ACT_NAME = "metricAct"
  val ACTOR_ROOT_PATH = "/user/enrollAct"
  val TOPIC_ROUTER_ACT_NAME = "topicRouterAct"
  val WAL_ROUTER_ACT_NAME = "walRouterAct"
  val DATA_CARRIER_ACT_NAME = "dataCarrierAct"
  val DATA_CARRIER_FILE_ACT_NAME = "data-carrier"

  val FindExistentTopics = "findExistentTopics"
  val CheckFile = "checkFile"
  val CheckFileFinished = "checkFileFinished"
  val Init = "init"
  val InitFinished = "initFinished"

  val DEFAULT_CLEANUP_INTERVAL = 1000 * 30

  val DEFAULT_MAX_FILE_LENGTH: Int = 1024 * 1024 * 100
  val DEFAULT_MAX_WRITE_BATCH_SIZE = 1024 * 1024 * 8
  val WAL_PREFIX = "db-"
  val WAL_SUFFIX = ".log"
  val TOPIC_PREFIX = "db-"
  val TOPIC_SUFFIX = ".log"

  //wal文件遵循的格式
  val INT_LENGTH = 4
  val LONG_LENGTH = 8
  val BYTE_LENGTH = 1
  val BATCH_TYPE = BYTE_LENGTH
  val BATCH_HEAD_INT = INT_LENGTH//批量消息头所占长度26个byte
  val CONTENT_LENGTH = INT_LENGTH//批量消息内容长度
  val BATCH_HEAD_RECORD_MAGIC = bytes("BATCH HEAD")
  val BATCH_HEAD_RECORD_MAGIC_LENGTH = BATCH_HEAD_RECORD_MAGIC.length
  val BATCH_TAIL_RECORD_MAGIC = bytes("BATCH TAIL")
  val BATCH_TAIL_RECORD_MAGIC_LENGTH = BATCH_TAIL_RECORD_MAGIC.length
  val CHECKSUM_LENGTH = LONG_LENGTH//校验码的长度
  val BATCH_CONTROL_RECORD_TYPE = 2 //批量包的类型
  val RECORD_HEAD_SPACE = BATCH_HEAD_INT + BATCH_TYPE//批量包的头长度(1个int 固定值28) + 批量包的类型 (1个byte)
  val BATCH_CONTROL_RECORD_SIZE = RECORD_HEAD_SPACE + BATCH_HEAD_RECORD_MAGIC_LENGTH + CONTENT_LENGTH + LONG_LENGTH
  val BATCH_CONTROL_RECORD_HEADER = {
    try {
      val os = new DataByteArrayOutputStream
      os.write(BATCH_CONTROL_RECORD_SIZE)
      os.writeByte(BATCH_CONTROL_RECORD_TYPE)
      os.write(BATCH_HEAD_RECORD_MAGIC)
      val sequence = os.toByteSequence
      sequence.compact
      sequence.getData
    }catch {
      case e:IOException => throw new RuntimeException("无法创建批量消息头!")
    }
  }
  val BATCH_CONTROL_RECORD_HEADER_LENGTH = BATCH_CONTROL_RECORD_HEADER.length
  val walAccessorPool = new DataFileAccessorPool


  //topic文件遵循的格式
  val MAGIC_HEAD = Array[Byte](10, 10)
  val MAGIC_HEAD_LENGTH = MAGIC_HEAD.length
  val MAGIC_TAIL = Array[Byte](20, 30, 30, 30, 30, 20)
  val MAGIC_TAIL_LENGTH = MAGIC_TAIL.length

  val topicAccessorPool = new DataFileAccessorPool


  def bytes(str:String) = {
    try {
      str.getBytes("UTF-8")
    }catch {
      case e:UnsupportedEncodingException => throw new DolphinException(e)
    }
  }

  case class FileWidget(prefix:String, suffix:String, path:String)

  trait FileRouter {

    def getFileIndex(file: File)(implicit widget:FileWidget) = {
      val name = file.getName
      name.substring(widget.prefix.length, name.length - widget.suffix.length).toInt
    }

    def createNewFile(children:immutable.Iterable[ActorRef])(implicit widget:FileWidget) = {
      val seq = children.toSeq
      val nextNum = if (seq.isEmpty) 1 else seq.head.path.name.toInt + 1
      val file = new File(widget.path, widget.prefix + org.dolphin.common.generateStrId(nextNum) + widget.suffix)
      if (!file.exists()) file.createNewFile()
      new DataFile(nextNum, file)
    }

    def getFileName(id:String)(implicit widget:FileWidget)  = {
      widget.prefix + id + widget.suffix
    }

    def getFilePath(id:String)(implicit widget:FileWidget) = {
      widget.path + "/" + getFileName(id)
    }

    def isFileAvailable(name:String)(implicit widget:FileWidget) = {
      name.startsWith(widget.prefix) && name.endsWith(widget.suffix)
    }
  }
}


