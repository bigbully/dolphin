package org.dolphin.broker.actor

import akka.actor.Actor
import org.dolphin.broker.mail.WriteDataCarrierFile
import java.io.RandomAccessFile
import org.dolphin.common._

/**
 * User: bigbully
 * Date: 14-6-7
 * Time: 下午11:21
 */
class DataCarrierFileAct(dataCarrierFile:RandomAccessFile) extends Actor{
  override def receive: Actor.Receive = {
    case WriteDataCarrierFile(walFileId, readOffset) => {
      val str = walFileId + "|" + readOffset + "\r\n"
      dataCarrierFile.seek(0)
      dataCarrierFile.write(str.getBytes(UTF_8))
    }
  }
}
