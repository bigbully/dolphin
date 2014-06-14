package org.dolphin.client.actor

import akka.actor.{Props, Actor}
import org.dolphin.client.mail.CreateClient
import scala.concurrent.Future
import org.dolphin.client.producer.{SyncProducer, AsyncProducer, Producer}

/**
 * User: bigbully
 * Date: 14-5-24
 * Time: 下午5:07
 */
class ClientRouterAct extends Actor{
  import context._

  override def receive: Actor.Receive = {
    case CreateClient(client) => {
      client match {
        case p:Producer => {
          p match {
            case ap:AsyncProducer => {//异步生产者所有操作均为异步
              ap.enrollActFuture = Future {
                actorOf(Props(classOf[EnrollAct], client, client.conf), client.id)
              }
            }
            case sp:SyncProducer =>
          }
        }
      }
    }
  }
}
