package org.dolphin.test


import akka.actor._
import akka.actor.ActorIdentity
import akka.actor.Identify
import scala.Some

/**
 * User: bigbully
 * Date: 14-5-29
 * Time: 下午10:58
 */


object MainAct {
  def main(args: Array[String]) {
    val system = ActorSystem("test")

    val mainAct = system.actorOf(Props(classOf[MainAct]), "main")
    val workAct = system.actorOf(Props(classOf[WorkAct], 1), "1")
    mainAct ! "Start"
  }
}

class WorkAct(id: Int) extends Actor {
  override def receive: Actor.Receive = {
    case Identify(msgId) => sender ! ActorIdentity(id, Some(self))
    case "SUCCESS" => println("yes")
  }
}

class MainAct extends Actor {

  import context._

  override def receive: Actor.Receive = {
    case ActorIdentity(id, Some(ref)) => {
      println("my id is " + id)
      ref ! "SUCCESS"
    }
    case ActorIdentity(id, None) => println("nonono")
    case "Start" => {
      println("start")
      actorSelection("/user/1") ! Identify(1)
    }
  }
}


