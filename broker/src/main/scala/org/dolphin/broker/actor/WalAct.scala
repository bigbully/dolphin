package org.dolphin.broker.actor

import akka.actor.{ActorIdentity, Identify, ActorLogging, Actor}
import org.dolphin.broker.store._
import org.dolphin.broker._
import java.io.{RandomAccessFile, IOException}
import java.util.zip.Adler32
import WalAct._
import java.util.{TimerTask, Timer}
import org.dolphin.mail.SendBatchMessage
import org.dolphin.broker.mail.{ReturnDataFile, GetDataFile}

/**
 * User: bigbully
 * Date: 14-5-24
 * Time: 下午3:12
 */
class WalAct(val dataFile:DataFile) extends Actor with ActorLogging{
  import context._

  val timer = new Timer("all Journal Scheduler")
  val task = new TimerTask {

    override def run {
      accessorPool.disposeUnused
    }
  }
  timer.scheduleAtFixedRate(task, DEFAULT_CLEANUP_INTERVAL, DEFAULT_CLEANUP_INTERVAL)

  override def receive: Actor.Receive = {
    case Identify(GetDataFile(readOffset)) => sender ! ActorIdentity(ReturnDataFile(dataFile, readOffset), Some(self))
    case CheckFile => {
      recoveryCheck(dataFile)
      parent ! CheckFileFinished
    }
  }

  def recoveryCheck(file: DataFile){
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
    if (file.isCorrupted) recover(file)

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

  def recover(corruptedBlocks:List[(Int, Int)], length:Int, reader:DataFileAccessor, file:RandomAccessFile){
    corruptedBlocks match {
      case corruptedBlock :: Nil => {
        recoverEachCorruptedBlock(corruptedBlock, (length, length), reader, file)
      }
      case corruptedBlock :: nextCorruptedBlock :: others => {
        recoverEachCorruptedBlock(corruptedBlock, nextCorruptedBlock, reader, file)
        recover(corruptedBlocks.tail, length, reader, file)
      }
    }
  }

  def recoverEachCorruptedBlock(corruptedBlock:(Int, Int), nextCorruptedBlock:(Int, Int), reader:DataFileAccessor, file:RandomAccessFile) = {
    var readOffset = corruptedBlock._2 + 1
    var writeOffset = corruptedBlock._1

    while (readOffset < nextCorruptedBlock._1){
      val sizeBytes = new Array[Byte](INT_LENGTH)
      reader.readFully(readOffset, sizeBytes)
      val sizeIs = new DataByteArrayInputStream(sizeBytes)
      val size = sizeIs.readInt

      val eachBatchLength = BATCH_CONTROL_RECORD_HEADER_LENGTH + size + BATCH_TAIL_RECORD_MAGIC_LENGTH
      val eachBatch = new Array[Byte](eachBatchLength)
      //每读一批消息，写入这批消息
      reader.readFully(readOffset, eachBatch)
      file.seek(writeOffset)
      file.write(eachBatch)
      //准备继续读下一批消息
      readOffset += eachBatchLength
      writeOffset += eachBatchLength
    }
  }

  //从文件中去掉corrupted的片段，把完好的片段衔接起来
  def recover(file: DataFile):DataFile = {
    val reader = accessorPool.openDataFileAccessor(file)
    val randomAccessFile = file.openRandomAccessFile
    try {
      val corruptedBlocks = file.corruptedBlocks.reverse
      recover(corruptedBlocks, file.getLength, reader, randomAccessFile)
    }finally {
      accessorPool.closeDataFileAccessor(reader)
      randomAccessFile.close()
    }
    file
  }
}

