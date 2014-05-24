package org.dolphin.broker.actor

import akka.actor.{Props, ActorLogging, Actor}
import org.dolphin.broker._
import java.io.{RandomAccessFile, IOException, File}
import org.dolphin.broker.store._
import scala.collection.SortedSet
import scala.collection.mutable.ArrayBuffer
import java.util.zip.Adler32
import org.dolphin.common._
import java.util.concurrent.atomic.AtomicInteger

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
  var waitingToBeCheck:AtomicInteger = _

  override def receive: Actor.Receive = {
    case Init => initWalFiles
    case CheckFileFinished => {
      val remainder = waitingToBeCheck.decrementAndGet()
      if (remainder == 0){
        parent ! InitFinished
      }
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
  }

  def getFileIndex(file: File) = {
    val name = file.getName
    name.substring(FILE_PREFIX.length, name.length - FILE_SUFFIX.length).toInt
  }

  def createNewFile = {
    val seq = children.toSeq
    val nextNum = if (seq.isEmpty) 1 else seq.head.path.name.toInt + 1
    val file = new File(walDir, FILE_PREFIX + nextNum + FILE_SUFFIX)
    if (!file.exists()) file.createNewFile()
    new DataFile(nextNum, file)
  }

}
