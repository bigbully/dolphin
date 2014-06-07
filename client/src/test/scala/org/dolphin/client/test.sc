import akka.actor._
import akka.actor.ActorIdentity
import akka.actor.Identify
import scala.Some
class WorkAct(id:Int) extends Actor {
  override def receive: Actor.Receive = {
    case Identify(msgId) => sender ! ActorIdentity(id, Some(self))
    case "SUCCESS" => println("yes")
  }
}
class MainAct extends Actor{
  import context._

  override def receive: Actor.Receive = {
    case ActorIdentity(id, Some(ref)) => ref ! "SUCCESS"
    case ActorIdentity(id, None) => println("nonono")
    case "Start" => {
      println("start")
      actorSelection("/user/1") ! Identify(1)
    }
  }
}

val system = ActorSystem("test")

val mainAct = system.actorOf(Props(classOf[MainAct]), "main")
val workAct = system.actorOf(Props(classOf[WorkAct], 1), "1")
mainAct ! "Start"