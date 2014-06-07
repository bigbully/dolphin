package org.dolphin.test

import akka.actor.Actor

/**
 * User: bigbully
 * Date: 14-5-29
 * Time: 下午11:32
 */
class WorkerAct extends Actor{
  override def receive: Actor.Receive = {
    case _ => println("abc")
  }


}
