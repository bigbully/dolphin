package org.dolphin.domain

import java.util.concurrent.atomic.AtomicLong

/**
 * User: bigbully
 * Date: 14-5-2
 * Time: 下午7:24
 */
class TopicHeartBeat(val name:String) {

  private[this] var produceSum = new AtomicLong(0L)
  private[this] var consumeSum = new AtomicLong(0L)
  private[this] var total = new AtomicLong(0L)
}
