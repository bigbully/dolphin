package org.dolphin.test

import akka.routing.{Router, Group}
import akka.actor.ActorSystem
import scala.collection.immutable.Iterable

/**
 * User: bigbully
 * Date: 14-6-1
 * Time: 下午9:42
 */
class MyGroup extends Group{
  override def paths: Iterable[String] = ???

  override def routerDispatcher: String = ???

  override def createRouter(system: ActorSystem): Router = ???
}
