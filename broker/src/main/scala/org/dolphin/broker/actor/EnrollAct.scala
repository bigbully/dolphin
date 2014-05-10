package org.dolphin.broker.actor

import akka.actor.{ActorRef, Props, ActorLogging, Actor}
import org.dolphin.domain.BrokerModel
import org.dolphin.common._
import org.dolphin.broker._
import org.dolphin.broker.mail.ExistentTopics
import org.dolphin.mail.{TopicCreated, CreateTopic, BrokerRegister}


/**
 * 与外界通信的act
 * User: bigbully
 * Date: 14-4-26
 * Time: 下午4:59
 */
class EnrollAct(brokerModel: BrokerModel, params: Map[String, String]) extends Actor with ActorLogging {

  import context._

  assert(params.contains("managerHost"))
  assert(params.contains("managerPort"))
  assert(params.contains("path"))
  val registryActPath = "akka.tcp://manager@" + params("managerHost") + ":" + params("managerPort") + "/user/" + REGISTRY_ACT_NAME
  var storeAct: ActorRef = _
  var metricAct: ActorRef = _

  override def receive: Actor.Receive = {
    case REGISTER => {
      log.info("查找现存的topic")
      storeAct ! FindExistentTopics
    }
    case ExistentTopics(list) => {
      log.info("向管理端发起注册请求:{}, 自带topic信息:{}", brokerModel, if (list.isEmpty) "无" else list)
      registryAct ! BrokerRegister(brokerModel, list)
    }

    case REGISTER_SUCCESS => log.info("成功连上管理端，注册成功!")
    case mail: CreateTopic => storeAct ! mail //创建topic
    case TopicCreated(topicModel, _) => registryAct ! TopicCreated(topicModel, brokerModel) //完成创建topic
  }

  def registryAct = {
    actorSelection(registryActPath)
  }

  @throws[Exception](classOf[Exception])
  override def preStart() {
    metricAct = actorOf(Props(classOf[MetricAct]), METRIC_ACT_NAME)
    storeAct = actorOf(Props(classOf[StoreAct], params), STORE_ACT_NAME)
  }
}
