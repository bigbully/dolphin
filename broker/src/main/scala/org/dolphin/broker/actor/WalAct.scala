package org.dolphin.broker.actor

import akka.actor.{ActorLogging, Actor}
import org.dolphin.broker._
import java.io.{IOException, File}
import org.dolphin.broker.store._
import scala.collection.SortedSet
import scala.collection.mutable.ArrayBuffer
import java.util.zip.Adler32

/**
 * User: bigbully
 * Date: 14-5-12
 * Time: 下午10:24
 */
class WalAct(storeParams: Map[String, String]) extends Actor with ActorLogging {
  val path = storeParams("path")
  var storeDir: File = _
  val walPath = path + "/journal"
  var walDir: File = _
  var started = false
  val accessorPool = new DataFileAccessorPool
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

  var sortedFiles = SortedSet.empty[DataFile]

  override def receive: Actor.Receive = {
    case Init => initWalFiles
  }

  //从文件中去掉corrupted的片段，把完好的片段衔接起来
  def recover(file: DataFile) {
    val reader = accessorPool.openDataFileAccessor(file)
    try {
      val corruptedBlocks = file.corruptedBlocks.reverse
      //todo
    }finally {
      accessorPool.closeDataFileAccessor(reader)
    }

  }

  def recoveryCheck(file: DataFile):DataFile = {
    var offset = 0

    val reader = accessorPool.openDataFileAccessor(file)
    try {
      while(true){
        val size = checkBatchRecord(reader, 0)
        if (size > 0){//如果这批消息没有问题
          offset = offset + BATCH_CONTROL_RECORD_SIZE + size + BATCH_TAIL_RECORD_MAGIC_LENGTH
        }else {//如果有问题
          val nextOffset = findNextBatchRecord(reader, offset + 1)
          if(nextOffset >= 0){
            log.info("wal文件出现corrupt:offsets between {} - {}", offset, nextOffset)
            file.addCorruptedBlocks(offset, nextOffset)
            offset = nextOffset
          }else {
            return file
          }
        }
      }
    }catch {
      case e :IOException => //ignore
    }finally {
      accessorPool.closeDataFileAccessor(reader)
    }
    file.setLength(offset)
    if (file.isCorrupted){
      recover(file)
    }
    file
  }

  def findNextBatchRecord(reader: DataFileAccessor,initialOffset: Int):Int = {
    var offset = initialOffset
    val header = new ByteSequence(BATCH_CONTROL_RECORD_HEADER)
    val data = new Array[Byte](1024 * 4)
    var bs = new ByteSequence(data, 0, reader.read(offset, data))

    var pos = 0
    while (true){
      pos = bs.indexOf(header, pos)
      if (pos >= 0) {//如果data中有header格式相同的bytes
        return offset + pos
      }else {
        if (bs.length != data.length){//如果读到的bytes没有达到1024*4的长度，说明eof了
          return -1
        }
        offset += bs.length - BATCH_CONTROL_RECORD_HEADER_LENGTH
        bs = new ByteSequence(data, 0, reader.read(offset, data))
        pos = 0
      }
    }
    -1//不可能到达这里
  }

  def checkBatchRecord(reader:DataFileAccessor, offset:Int):Int = {
    //验证batch头
    val controlRecord = new Array[Byte](BATCH_CONTROL_RECORD_SIZE)
    reader.readFully(offset, controlRecord)
    val controlIs = new DataByteArrayInputStream(controlRecord)
    (0 until BATCH_CONTROL_RECORD_HEADER.length).foreach(i => if (controlIs.readByte != BATCH_CONTROL_RECORD_HEADER(i)) return -1)
    val size = controlIs.readInt
    if (size > DEFAULT_MAX_WRITE_BATCH_SIZE) return -1

    //验证checksum
    val expectedChecksum = controlIs.readLong
    val data = new Array[Byte](size)
    reader.readFully(offset + BATCH_CONTROL_RECORD_SIZE, data)
    val checksum = new Adler32
    checksum.update(data, 0, data.length)
    if (expectedChecksum != checksum) return -1

    //验证batch尾
    val batchTail = new Array[Byte](BATCH_TAIL_RECORD_MAGIC_LENGTH)
    val batchTailIs = new DataByteArrayInputStream(batchTail)
    reader.readFully(offset+BATCH_CONTROL_RECORD_SIZE+size, batchTail)
    (0 until BATCH_TAIL_RECORD_MAGIC_LENGTH).foreach(i => if (batchTailIs.readByte != BATCH_TAIL_RECORD_MAGIC(i)) return -1)

    size
  }

  def initWalFiles {
    storeDir = new File(path)
    if (!storeDir.exists()) storeDir.mkdir()

    val startTime = System.currentTimeMillis()

    walDir = new File(walPath)
    if (!walDir.exists()) walDir.mkdir()
    val files = walDir.listFiles.filter(file => file.isFile && file.getName.startsWith(FILE_PREFIX) && file.getName.endsWith(FILE_SUFFIX)).toSeq


    //获得一个从大到小排列的sortedSet
    files match {
      case Nil => sortedFiles += createNewFile
      case _ => {
        files.foreach(file => {
          sortedFiles += recoveryCheck(new DataFile(getFileIndex(file), file))
        })
      }
    }

    started = true
    val endTime = System.currentTimeMillis()
    log.info("存储模块初始化完成，共耗时{}ms", endTime - startTime)
  }

  def getFileIndex(file: File) = {
    val name = file.getName
    name.substring(FILE_PREFIX.length, name.length - FILE_SUFFIX.length).toInt
  }

  def createNewFile = {
    val nextNum = if (sortedFiles.isEmpty) 1 else sortedFiles.head.id + 1
    val file = new File(walDir, FILE_PREFIX + nextNum + FILE_SUFFIX)
    if (!file.exists()) file.createNewFile()
    new DataFile(nextNum, file)
  }

}
