package org.dolphin.test

import akka.routing.{RandomRoutingLogic, Resizer, Router, Pool}
import akka.actor.{ActorSelection, SupervisorStrategy, ActorSystem}
import akka.dispatch.Dispatchers

/**
 * User: bigbully
 * Date: 14-5-31
 * Time: 上午9:50
 */
case class MyPool(instances:Int) extends Pool{

  var router = new Router(RandomRoutingLogic())
  override def supervisorStrategy: SupervisorStrategy = Pool.defaultSupervisorStrategy

  override def resizer: Option[Resizer] = None

  override def nrOfInstances: Int = instances

  override def routerDispatcher: String = Dispatchers.DefaultDispatcherId

  override def createRouter(system: ActorSystem): Router = router

  def addRoutee(sel:ActorSelection) {
    router.addRoutee(sel)
  }
  def getRoutee = {
    router.routees
  }
}
