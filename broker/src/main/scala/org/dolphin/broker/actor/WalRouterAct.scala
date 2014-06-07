package org.dolphin.broker.actor

import akka.actor.{Props, ActorLogging, Actor}
import org.dolphin.broker._
import java.io.{InterruptedIOException, RandomAccessFile, IOException, File}
import org.dolphin.broker.store._
import java.util.zip.Adler32
import org.dolphin.common._
import java.util.concurrent.atomic.{AtomicReference, AtomicInteger}
import org.dolphin.domain.Message
import java.util.concurrent.locks.ReentrantLock
import org.dolphin.Util.DolphinException
import java.util.concurrent.CountDownLatch


/**
 * User: bigbully
 * Date: 14-5-12
 * Time: 下午10:24
 */
class WalRouterAct(storeParams: Map[String, String]) extends Actor with ActorLogging {

  import context._

  val path = storeParams("path")
  var storeDir: File = _
  val walPath = path + "/journal"
  var walDir: File = _
  var waitingToBeCheck: AtomicInteger = _

  val lock = new ReentrantLock()
  val condition = lock.newCondition()
  var shutdown = false
  var running = false
  var processQueueThread: Thread = _
  var firstAsyncException: Exception = _

  val shutdownDone = new CountDownLatch(1)
  var nextWriteBatch: Option[WriteBatch] = None

  override def receive: Actor.Receive = {
    case Message(content, topic, subTopic, sync) => {
      //todo callback
      val batch = enqueue(WriteCommand(content, topic, subTopic, sync))

      if (sync) {
        try {
          batch.get.latch.await()
        } catch {
          case e: InterruptedException => throw new InterruptedIOException()
        }
        val exception = batch.get.exception.get
        if (exception != null) throw exception //todo 处理这个异常
      }
    }
    case Init => initWalFiles
    case CheckFileFinished => {
      val remainder = waitingToBeCheck.decrementAndGet()
      if (remainder == 0) {
        parent ! InitFinished
      }
    }
  }

  def enqueue(write: WriteCommand) = {
    lock.lock()
    try {
      if (shutdown) throw new DolphinException("Async Writter Thread Shutdown")
      if (!running) {
        processQueueThread = new Thread() {
          override def run() {
            processQueue
          }
        }
        processQueueThread.setPriority(Thread.MAX_PRIORITY)
        processQueueThread.setDaemon(true) //设为守护线程，主线程结束之后即停止
        processQueueThread.setName("wal Data File Writer")
        processQueueThread.start()
        firstAsyncException = null
      }
      if (firstAsyncException != null) throw firstAsyncException

      recursiveEnqueue(write)
    } finally {
      lock.unlock()
    }
    nextWriteBatch
  }

  def recursiveEnqueue(write: WriteCommand) {
    nextWriteBatch match {
      case None => {
        //如果还未初始化batch或当前batch已经被取走，则创建一个batch
        var file = getCurrentWriteFile
        if (file.getLength > DEFAULT_MAX_FILE_LENGTH) file = createNewFile
        nextWriteBatch = Some(new WriteBatch(file, file.getLength, write))
        condition.notifyAll //并通知等待的线程
      }
      case Some(writeBatch) => {
        if (writeBatch.canAppend(write)) {
          //如果batch允许继续append
          writeBatch.append(write)
        } else {
          //如果不能，则等待batch被取走
          try {
            while (nextWriteBatch != null) condition.wait()
          } catch {
            case e: InterruptedException => throw new InterruptedIOException()
          }
          if (shutdown) throw new IOException("Async Writter Thread Shutdown")
          recursiveEnqueue(write) //当旧的batch被取走之后，把writeCommand添加到新的batch中
        }
      }
    }
  }

  def getCurrentWriteFile = {
    this.synchronized {
      val fileId = children.toSeq.head.path.name
      val file = new File(getWalFilePath(walPath, getWalFileName(fileId)))
      new DataFile(fileId.toInt, file)
    }
  }

  def processQueue {

    var dataFile: DataFile = null
    var file: RandomAccessFile = null
    var wb: WriteBatch = null
    try {
      val buff: DataByteArrayOutputStream = new DataByteArrayOutputStream(DEFAULT_MAX_WRITE_BATCH_SIZE)
      while (true) {
        lock.lock()
        try {
          wb = exchange
        } finally {
          condition.notifyAll()
          lock.unlock()
        }

        if (dataFile != wb.dataFile) {
          if (file != null) {
            file.setLength(dataFile.getLength)
            dataFile.closeRandomAccessFile(file)
          }
          dataFile = wb.dataFile;
          file = dataFile.openRandomAccessFile
          if (file.length() < DEFAULT_MAX_FILE_LENGTH) file.setLength(DEFAULT_MAX_FILE_LENGTH)
        }

        buff.reset
        buff.writeInt(BATCH_CONTROL_RECORD_SIZE)
        buff.writeByte(BATCH_CONTROL_RECORD_TYPE)
        buff.write(BATCH_HEAD_RECORD_MAGIC)
        buff.writeInt(0)
        buff.writeLong(0)

        recursiveWriteToBuff(wb.getWrites, buff)

        buff.write(BATCH_TAIL_RECORD_MAGIC)

        val sequence = buff.toByteSequence

        buff.reset
        buff.skip(RECORD_HEAD_SPACE + BATCH_HEAD_RECORD_MAGIC_LENGTH)
        buff.writeInt(sequence.getLength - BATCH_CONTROL_RECORD_SIZE - BATCH_TAIL_RECORD_MAGIC_LENGTH)

        val checksum = new Adler32()
        checksum.update(sequence.getData, sequence.getOffset + BATCH_CONTROL_RECORD_SIZE, sequence.getLength - BATCH_CONTROL_RECORD_SIZE - BATCH_TAIL_RECORD_MAGIC_LENGTH)
        buff.writeLong(checksum.getValue())

        file.write(sequence.getData, sequence.getOffset, sequence.getLength)

        //不强制刷盘
        //file.getFD().sync();

        wb.latch.countDown();
      }
    } catch {
      case e: IOException => {
        lock.lock()
        try {
          firstAsyncException = e
          if (wb != null) {
            wb.exception.set(e)
            wb.latch.countDown()
          }
          nextWriteBatch match {
            case Some(batch) => {
              batch.exception.set(e)
              batch.latch.countDown()
            }
          }
        } finally {
          lock.unlock()
        }
      }
      case e: InterruptedException =>
    } finally {
      try {
        if (file != null) {
          dataFile.closeRandomAccessFile(file)
        }
      } catch {
        case e: Throwable => //ignore
      }
      shutdownDone.countDown()
      running = false
    }
  }

  def recursiveWriteToBuff(writeList: List[WriteCommand], buff: DataByteArrayOutputStream) {
    writeList match {
      case head :: tails => {
        buff.writeInt(head.topicSize)
        buff.write(head.topic)
        buff.writeInt(head.subTopicSize)
        head.subTopic match {
          case Some(sub) => buff.write(sub)
        }
        buff.writeInt(head.size)
        buff.write(head.data)
        recursiveWriteToBuff(tails, buff)
      }
    }
  }

  def exchange: WriteBatch = {
    nextWriteBatch match {
      case Some(batch) => {
        batch.dataFile.incrementLength(BATCH_TAIL_RECORD_MAGIC_LENGTH)
        val o = batch
        nextWriteBatch = None
        return o
      }
      case None => exchange
    }
    if (shutdown) {
      null
    } else {
      exchange
    }
  }


  def initWalFiles {
    storeDir = new File(path)
    if (!storeDir.exists()) storeDir.mkdir()

    walDir = new File(walPath)
    if (!walDir.exists()) walDir.mkdir()
    val files = walDir.listFiles.filter(file => file.isFile && file.getName.startsWith(FILE_PREFIX) && file.getName.endsWith(FILE_SUFFIX)).toSeq

    //获得一个从大到小排列的sortedSet
    files match {
      case Nil => {
        val newDataFile = createNewFile
        actorOf(Props(classOf[WalAct], newDataFile), generateStrId(newDataFile.id))
      }
      case _ => {
        waitingToBeCheck = new AtomicInteger(files.length)
        files.foreach(file => {
          val dataFile = new DataFile(getFileIndex(file), file)
          actorOf(Props(classOf[WalAct], dataFile), generateStrId(dataFile.id)) ! CheckFile
        })
      }
    }
    log.info("wal文件初始化完成!当前存在wal文件为:{}", children)
  }

  def getFileIndex(file: File) = {
    val name = file.getName
    name.substring(FILE_PREFIX.length, name.length - FILE_SUFFIX.length).toInt
  }

  def createNewFile = {
    val seq = children.toSeq
    val nextNum = if (seq.isEmpty) 1 else seq.head.path.name.toInt + 1
    val file = new File(walDir, FILE_PREFIX + generateStrId(nextNum) + FILE_SUFFIX)
    if (!file.exists()) file.createNewFile()
    new DataFile(nextNum, file)
  }

  private case class WriteCommand(data: Array[Byte], topic: Array[Byte], subTopic: Option[Array[Byte]], sync: Boolean) {
    val size = data.length
    val topicSize = topic.length
    val subTopicSize = subTopic match {
      case None => 0
      case Some(sub) => sub.length
    }
  }

  private case class WriteBatch(dataFile: DataFile, offset: Int, initialWrite: WriteCommand) {
    var size = BATCH_CONTROL_RECORD_SIZE
    private var writes = List.empty[WriteCommand]
    var totalNum = 0
    var exception = new AtomicReference[IOException]
    val latch = new CountDownLatch(1)
    lazy val getWrites = writes.reverse

    dataFile.incrementLength(BATCH_CONTROL_RECORD_SIZE)
    append(initialWrite)

    def append(write: WriteCommand) {
      writes ::= write
      size += write.size
      dataFile.incrementLength(write.size)
      totalNum += 1
    }

    def canAppend(write: WriteCommand) = {
      val newSize = size + write.size
      newSize >= DEFAULT_MAX_WRITE_BATCH_SIZE ||
        offset + newSize + BATCH_TAIL_RECORD_MAGIC_LENGTH > DEFAULT_MAX_WRITE_BATCH_SIZE
    }


  }

}
