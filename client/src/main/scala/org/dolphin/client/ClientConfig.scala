package org.dolphin.client

import org.dolphin.Util.DolphinException
import scala.Some

/**
 * User: bigbully
 * Date: 14-4-26
 * Time: 下午6:10
 */
case class ClientConfig(host: String, port: Int, param: (String, Any)*) {
  var map = Map.empty[String, Any]
  param.foreach(kv => {
    map += kv
  })

  /**
   * 发送消息的方式
   * async为异步
   * sync为同步
   */
  map.get(PRODUCER_SEND_MODE) match {
    case None => map += (PRODUCER_SEND_MODE -> ASYNC)
    case Some(str: String) if (str.equalsIgnoreCase(ASYNC) || str.equalsIgnoreCase(SYNC)) =>
    case _ => throw new DolphinException(PRODUCER_SEND_MODE + "只能设置为:" + ASYNC + "或" + SYNC)
  }

  /**
   * 消息推入队列的超时时间:
   * 0: 如果队列满，则消息立即抛弃
   * -: 如果队列满则阻塞
   * +: 如果队列满则等待producer.enqueue.timeout.ms
   */
  map.get(PRODUCER_ENQUEUE_TIMEOUT_MS) match {
    case None => map += (PRODUCER_ENQUEUE_TIMEOUT_MS -> -1)
    case Some(num: Int) =>
    case _ => throw new DolphinException(PRODUCER_ENQUEUE_TIMEOUT_MS + "只能设置为整数")
  }

  /**
   * 批量消息在队列中停留的最长时间
   */
  map.get(QUEUE_BUFFERING_MAX_MS) match {
    case None => map += (QUEUE_BUFFERING_MAX_MS -> 5000)
    case Some(num: Int) if (num > 0) =>
    case _ => throw new DolphinException(QUEUE_BUFFERING_MAX_MS + "只能设置为正整数")
  }

  /**
   * 一批包含多少条消息
   */
  map.get(BATCH_NUM_MESSAGES) match {
    case None => map += (BATCH_NUM_MESSAGES -> 200)
    case Some(num: Int) if (num > 0) =>
    case _ => throw new DolphinException(BATCH_NUM_MESSAGES + "只能设置为正整数")
  }

  /**
   * 批量消息在队列中停留的最大消息量
   */
  map.get(QUEUE_BUFFERING_MAX_MESSAGES) match {
    case None => map += (QUEUE_BUFFERING_MAX_MESSAGES -> 10000)
    case Some(num: Int) if (num > 0) =>
    case _ => throw new DolphinException(QUEUE_BUFFERING_MAX_MESSAGES + "只能设置为正整数")
  }

  /**
   * 发送失败之后的后续处理
   * "ex":直接抛异常
   * "sync":同步发送给其他broker
   * "async":异步发送给其他broker
   */
  map.get(FAIL_TO_SEND_AND_THEN) match {
    case None => map += (FAIL_TO_SEND_AND_THEN -> SYNC)
    case Some(str: String) if (str.equalsIgnoreCase("ex") || str.equalsIgnoreCase(ASYNC) || str.equalsIgnoreCase(SYNC)) =>
    case _ => throw new DolphinException(FAIL_TO_SEND_AND_THEN + "只能设置为:" + ASYNC + "或" + SYNC + "或ex")
  }

  /**
   * 向broker发送的策略
   * "round-robin":默认轮询发送
   * "random":随机发送
   * "smallest-mailbox":向mailbox中最少消息的broker发送
   */
  map.get(SEND_STRATEGY) match {
    case None => map += (SEND_STRATEGY -> "round-robin")
    case Some(str: String) if (str.equalsIgnoreCase(ROUND_ROBIN) || str.equalsIgnoreCase(RANDOM) || str.equalsIgnoreCase(SMALLEST_MAILBOX)) =>
    case _ => throw new DolphinException(SEND_STRATEGY + "只能设置为:" + ROUND_ROBIN + "或" + RANDOM + "或" + SMALLEST_MAILBOX)
  }

  def get(name: String) = {
    map.get(name)
  }
}
