package org.dolphin

import java.io.{IOException, UnsupportedEncodingException}
import org.dolphin.Util.DolphinException
import org.dolphin.broker.store.{DataFileAccessorPool, DataByteArrayOutputStream}

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
  val GetDataFile = "getDataFile"

  val DEFAULT_CLEANUP_INTERVAL = 1000 * 30

  val DEFAULT_MAX_FILE_LENGTH: Int = 1024 * 1024 * 100
  val DEFAULT_MAX_WRITE_BATCH_SIZE = 1024 * 1024 * 8
  val FILE_PREFIX = "db-"
  val FILE_SUFFIX = ".log"

  //wal文件遵循的协议格式
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

  //wal文件专用
  val accessorPool = new DataFileAccessorPool

  def bytes(str:String) = {
    try {
      str.getBytes("UTF-8")
    }catch {
      case e:UnsupportedEncodingException => throw new DolphinException(e)
    }
  }

  def getWalFileName(id:String) = {
    FILE_PREFIX + id + FILE_SUFFIX
  }
  def getWalFilePath(path:String, fileName:String) = {
    path + "/" + fileName
  }
}
