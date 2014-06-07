package org.dolphin.broker.actor

import akka.actor._
import org.dolphin.broker._
import org.dolphin.common._
import java.io.{EOFException, IOException, RandomAccessFile}
import org.dolphin.broker.store.{DataByteArrayInputStream, DataFile, DataFileAccessor}
import java.util.zip.{Adler32, Checksum}
import org.dolphin.broker.mail.{WriteDataCarrierFile, DistributeToTopic}
import akka.actor.ActorIdentity
import scala.Some
import akka.actor.Identify

/**
 * User: bigbully
 * Date: 14-6-6
 * Time: 下午10:53
 */
class DataCarrierAct(storeParams: Map[String, String]) extends Actor with ActorLogging {

  import context._

  val dataCarrierFile = new RandomAccessFile(storeParams.get("path") + "/journal/"+DATA_CARRIER_FILE_ACT_NAME + FILE_SUFFIX, "rw")
  var walFileId: String = _
  var readOffset: Long = _
  var curDataFileAccessor: DataFileAccessor = _
  var dataCarrierFileAct:ActorRef = _

  override def receive: Actor.Receive = {
    case Init => {
      val dataCarrierInfo = dataCarrierFile.readLine()
      if (dataCarrierInfo != null) {
        val infoArray = dataCarrierInfo.split("\\|")
        walFileId = infoArray(0)
        readOffset = infoArray(1).toLong
        curWalAct ! Identify(walFileId)
        dataCarrierFileAct = actorOf(Props(classOf[DataCarrierFileAct], dataCarrierFile), DATA_CARRIER_FILE_ACT_NAME)
      }
    }
    case ActorIdentity(dataFile, Some(walAct)) => {
      accessorPool.openDataFileAccessor(dataFile.asInstanceOf[DataFile])
      curDataFileAccessor.getFile.seek(readOffset)

    }
    case ActorIdentity(_, None) => log.error("dataCarrier初始化失败，找不到编号为{}的wal文件!", name)
  }



  class CarrierThread extends Thread {
    this.setName("CarrierThread")

    override def run {
      while (true) {
        try {
          distribute(readBatchJournal)
          dataCarrierFileAct ! WriteDataCarrierFile(walFileId, readOffset)
        } catch {
          case eof: EOFException => //todo切换
        }
      }
    }
  }

  def distribute(stream: DataByteArrayInputStream) {
    var offset = 0
    while(offset < stream.getLength){
      val topicSize = stream.readInt
      val topicBytes = new Array[Byte](topicSize)
      stream.read(topicBytes)
      val topic = new String(topicBytes, UTF_8)
      val subTopicSize = stream.readInt
      val subTopic = subTopicSize match {
        case 0 => None
        case _ => {
          val subTopicBytes = new Array[Byte](stream.readInt)
          stream.read(subTopicBytes)
          Some(new String(subTopicBytes, UTF_8))
        }
      }
      val size = stream.readInt
      val data = new Array[Byte](size)
      topicAct(topic) ! DistributeToTopic(topic, subTopic, data)
      offset += 3 * INT_LENGTH + topicSize + subTopicSize + size
    }
  }

  def readBatchJournal: DataByteArrayInputStream = {
    //步骤1:读取批量消息头信息
    val head = new Array[Byte](BATCH_CONTROL_RECORD_HEADER_LENGTH)
    val headStream = new DataByteArrayInputStream(head)
    try {
      readOffset match {
        case 0 => curDataFileAccessor.readFully(0, head) //据说是为了解决文件系统可能落地慢的bug，未验证
        case _ => curDataFileAccessor.readFully(head)
      }
    } catch {
      case e: IOException => {
        curDataFileAccessor.getFile.seek(readOffset)
        throw new EOFException("读取head发生异常!")
      }
    }


    //步骤2:验证消息头是否正确
    var byte: Byte = 0
    for (b <- BATCH_CONTROL_RECORD_HEADER) {
      byte = headStream.readByte
      if (byte != b) {
        curDataFileAccessor.getFile.seek(readOffset)
        throw new ReadBatchException("BATCH_CONTROL_RECORD_HEADER ERROR")
      }
    }


    //步骤3:读出验证总数用的long
    val size = headStream.readInt()
    val checkSum = headStream.readLong()


    //步骤4:读出消息体，但不检查
    if (size > DEFAULT_MAX_WRITE_BATCH_SIZE || size <= 0) {
      curDataFileAccessor.getFile.seek(readOffset)
      throw new ReadBatchException("MAX_BATCH_SIZE ERROR")
    }

    val data = new Array[Byte](size)
    try {
      curDataFileAccessor.readFully(data)
    } catch {
      case e: IOException => {
        curDataFileAccessor.getFile.seek(readOffset)
        throw new EOFException("DATA ERROR")
      }
    }


    //步骤5:验证消息尾是否正确
    val tail = new Array[Byte](BATCH_TAIL_RECORD_MAGIC_LENGTH)
    val tailStream = new DataByteArrayInputStream(tail)
    try {
      curDataFileAccessor.readFully(tail)
    } catch {
      case e: IOException => {
        curDataFileAccessor.getFile.seek(readOffset)
        throw new EOFException("BATCH_TAIL ERROR")
      }
    }

    for (b <- BATCH_TAIL_RECORD_MAGIC) {
      byte = tailStream.readByte
      if (byte != b) {
        curDataFileAccessor.getFile.seek(readOffset)
        throw new ReadBatchException("BATCH_TAIL_RECORD_MAGIC ERROR");
      }
    }

    //步骤6:验证验证消息体
    val stream = new DataByteArrayInputStream(data)
    if (!checkTheSum(data, checkSum)) {
      curDataFileAccessor.getFile.seek(readOffset)
      throw new ReadBatchException("CHECK_SUM ERROR")
    }

    stream.restart
    readOffset += BATCH_CONTROL_RECORD_SIZE + size + BATCH_TAIL_RECORD_MAGIC_LENGTH
    stream
  }

  def checkTheSum(data:Array[Byte], expect:Long) = {
    try {
      val checksum: Checksum = new Adler32
      checksum.update(data, 0, data.length)
      expect == checksum.getValue
    } catch {
      case e: Exception => false
    }
  }

  val TopicActPathPrefix = ACTOR_ROOT_PATH + "/" + STORE_ACT_NAME + "/" + TOPIC_ROUTER_ACT_NAME + "/"
  val CurWalActPathPrefix = ACTOR_ROOT_PATH + "/" + STORE_ACT_NAME + "/" + WAL_ROUTER_ACT_NAME + "/"

  def topicAct(topic:String) = actorSelection(TopicActPathPrefix + topic)
  def curWalAct = actorSelection(CurWalActPathPrefix + walFileId)


  class ReadBatchException(msg: String) extends Exception(msg)
}



