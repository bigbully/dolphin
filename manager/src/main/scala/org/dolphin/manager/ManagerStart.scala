package org.dolphin.manager

import akka.actor.{ActorRef, Props, ActorSystem}
import org.dolphin.manager.actor.{CollectAct, RegistryAct}
import org.quartz.{SimpleScheduleBuilder, ScheduleBuilder, TriggerBuilder, JobBuilder}
import org.dolphin.manager.util.CollectJob
import org.quartz.impl.StdSchedulerFactory
import org.dolphin.common._

object ManagerStart {

  val scheduler = StdSchedulerFactory.getDefaultScheduler
  scheduler.start();

  /**
   * 初始化
   * @param collectAct
   * @param period
   */
  def startCollectJob(collectAct: ActorRef, period: Int) {
    val jobDetail = JobBuilder.newJob(classOf[CollectJob]).withIdentity(COLLECT_ACT_NAME).build()
    jobDetail.getJobDataMap.put(COLLECT_ACT_NAME, collectAct)
    val trigger = TriggerBuilder.newTrigger()
      .withIdentity(COLLECT_ACT_NAME, COLLECT_ACT_NAME).startNow()
      .withSchedule(SimpleScheduleBuilder.simpleSchedule().withIntervalInSeconds(period).repeatForever())
      .build()
    scheduler.scheduleJob(jobDetail, trigger)
  }

  def main(args: Array[String]) {
    val system = ActorSystem("manager")
    val registryAct = system.actorOf(Props[RegistryAct], REGISTRY_ACT_NAME)
    val collectAct = system.actorOf(Props[CollectAct], COLLECT_ACT_NAME)
    val collectTime = 30 //秒
    startCollectJob(collectAct, collectTime);
  }

}

