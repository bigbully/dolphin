package org.dolphin.test

import akka.routing._
import akka.actor.{Props, SupervisorStrategy, ActorSystem}
import akka.routing.Router

/**
 * User: bigbully
 * Date: 14-5-29
 * Time: 下午11:00
 */
final case class MyRouter(val routingLogic: RoutingLogic, val supervisorStrategy: SupervisorStrategy = Pool.defaultSupervisorStrategy) extends CustomRouterConfig {

  override def createRouter(system: ActorSystem): Router = {
    new Router(routingLogic)
  }

  def props(routeeProps: Props): Props = routeeProps.withRouter(this)

  def addRoutee {

  }
}
