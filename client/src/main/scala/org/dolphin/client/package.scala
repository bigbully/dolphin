package org.dolphin

import java.lang.management.ManagementFactory
import java.net.InetAddress
import java.util.concurrent.atomic.AtomicLong

/**
 * User: bigbully
 * Date: 14-4-28
 * Time: 下午10:34
 */
package object client {

  val BROKER_GROUP_ACT_NAME = "brokerGroupAct"
  val BROKER_ROUTER_ACT_NAME = "brokerRouterAct"
  val CLIENT_ROUTER_ACT_NAME = "clientRouterAct"
  val ACTOR_ROOT_PATH = "/user/enrollAct"

  val PRODUCER_SEND_MODE = "producer.send.mode"
  val PRODUCER_ENQUEUE_TIMEOUT_MS = "producer.enqueue.timeout.ms"
  val QUEUE_BUFFERING_MAX_MS = "queue.buffering.max.ms"
  val QUEUE_BUFFERING_MAX_MESSAGES = "queue.buffering.max.messages"
  val BATCH_NUM_MESSAGES = "batch.num.messages"
  val FAIL_TO_SEND_AND_THEN = "fail.to.send.and.then"
  val SEND_STRATEGY = "send.strategy"
  val ROUND_ROBIN = "round-robin"
  val SMALLEST_MAILBOX = "smallest-mailbox"
  val RANDOM = "random"
  val ASYNC = "async"
  val SYNC = "sync"
  private val num = new AtomicLong(0)

  def generateId = {
    ManagementFactory.getRuntimeMXBean.getName + "-" + num.incrementAndGet()
  }
}
