package org.dolphin.manager.util

import org.quartz.{JobExecutionContext, Job}
import akka.actor.ActorRef
import org.dolphin.common._
import org.dolphin.manager._


/**
 * User: bigbully
 * Date: 14-4-29
 * Time: 下午8:09
 */
class CollectJob extends Job {

  override def execute(context: JobExecutionContext) {
    val collectAct = context.getJobDetail.getJobDataMap.get(COLLECT_ACT_NAME).asInstanceOf[ActorRef]
    collectAct ! COLLECT
  }
}
