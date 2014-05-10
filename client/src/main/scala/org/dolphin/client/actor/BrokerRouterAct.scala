package org.dolphin.client.actor

import akka.actor.{ActorLogging, Props, Actor}
import org.dolphin.client.mail.{BrokersOnlineFinished, BrokersOnline}

/**
 * User: bigbully
 * Date: 14-5-10
 * Time: 下午9:10
 */
class BrokerRouterAct extends Actor with ActorLogging{
  import context._

  val enrollAct = parent

  override def receive: Actor.Receive = {
    case BrokersOnline(list) => {
      var brokerIds = List.empty[Int]
      list.foreach{brokerModel => {
        child(brokerModel.id.toString) match {
          case None => {
            actorOf(Props(classOf[BrokerAct], brokerModel), brokerModel.id.toString)
            brokerIds ::= brokerModel.id
          }
        }
      }}
      enrollAct ! BrokersOnlineFinished(brokerIds.reverse)
    }
  }

}
