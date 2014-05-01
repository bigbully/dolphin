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

  private val num = new AtomicLong(0)

  def generateId = {
    ManagementFactory.getRuntimeMXBean.getName + "-" + num.incrementAndGet()
  }
}
