package org.dolphin.client.producer

import org.slf4j.LoggerFactory
import org.dolphin.common._
import org.dolphin.domain.{Message, ClientModel, TopicModel}
import org.dolphin.client.mail.RegisterClient
import java.util.concurrent.{TimeUnit, LinkedBlockingQueue}
import org.dolphin.client._
import org.dolphin.Util.DolphinException
import org.dolphin.client.mail.RegisterClient
import org.dolphin.client.ClientConfig
import org.dolphin.domain.ClientModel
import org.dolphin.domain.Message
import org.dolphin.domain.TopicModel
import scala.collection.mutable.ArrayBuffer
import org.dolphin.mail.SendBatchMessage

/**
 * User: bigbully
 * Date: 14-5-24
 * Time: 下午5:00
 */
class AsyncProducer(private[this] val id: String, private[this] val conf: ClientConfig) extends Producer(id, conf) {

  val log = LoggerFactory.getLogger(classOf[AsyncProducer])

  val queue = new LinkedBlockingQueue[Message](conf.get(QUEUE_BUFFERING_MAX_MESSAGES).asInstanceOf[Int])
  val enqueueStrategy: (Message => Unit) = conf.get(PRODUCER_ENQUEUE_TIMEOUT_MS) match {
    case 0 => msg => if (!queue.offer(msg)) throw new DolphinException("异步队列已满，抛弃这笔消息:" + new String(msg.content))
    case num:Int if (num < 0) => queue.put(_)
    case num:Int if (num > 0) => {
      val producerEnqueueTimeoutMs = conf.get(PRODUCER_ENQUEUE_TIMEOUT_MS).get.asInstanceOf[Int]
      msg => if(!queue.offer(msg, producerEnqueueTimeoutMs, TimeUnit.MILLISECONDS)) throw new DolphinException("异步队列已满，" + producerEnqueueTimeoutMs + "ms内推送到异步队列失败，抛弃这笔消息:" + new String(msg.content));
    }
  }
  val queueTime = conf.get(QUEUE_BUFFERING_MAX_MS).get.asInstanceOf[Int]
  var batchSize = conf.get(BATCH_NUM_MESSAGES).get.asInstanceOf[Int]

  val sendThread = new Thread(){
    override def run {
      processBatchMessage
    }
  }

  val shutdownCommand = Message("shutdown".getBytes())

  def startSendThread {
    sendThread.start()
  }

  //原封不动照搬kafka
  private def processBatchMessage {
    var lastSend = System.currentTimeMillis()
    var batch = new ArrayBuffer[Message]
    var full = false

    //从时间和消息量两个维度进行消息发送
    Stream.continually(queue.poll(scala.math.max(0, (lastSend + queueTime) - System.currentTimeMillis()), TimeUnit.MILLISECONDS))
    .takeWhile(item => if (item != null) item ne shutdownCommand else false)
    .foreach{ currentQueueItem =>
//      val elapsed = System.currentTimeMillis() - lastSend
      val expired = currentQueueItem == null
      if (currentQueueItem != null) batch += currentQueueItem

      full = batch.size >= batchSize
      if (full || expired){
        lastSend = System.currentTimeMillis()
        enrollAct ! SendBatchMessage(batch)
        batch = new ArrayBuffer[Message]
      }
    }
    //发送最后一批消息
    enrollAct ! SendBatchMessage(batch)
    if (queue.size > 0) throw new DolphinException("异步客户端结束异常，不应该有残留的消息!")
  }

  override def publish(topic: String, cluster: String) {
    val topicModel = TopicModel(topic, cluster)
    enrollAct ! RegisterClient(this, ClientModel(id, PRODUCER), topicModel)
  }

  override def send(msg: Array[Byte]) {
    enqueueStrategy(Message(msg))
  }
}
