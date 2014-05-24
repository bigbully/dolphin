package org.dolphin

import java.io.UnsupportedEncodingException
import org.dolphin.Util.DolphinException

/**
 * User: bigbully
 * Date: 14-5-6
 * Time: 上午12:18
 */
package object broker {
  val REGISTER = "register"
  val STORE_ACT_NAME = "storeAct"
  val METRIC_ACT_NAME = "metricAct"
  val ACTOR_ROOT_PATH = "/user/enrollAct"
  val TOPIC_ROUTER_ACT_NAME = "topicRouterAct"
  val WAL_ACT_NAME = "walAct"

  val FindExistentTopics = "findExistentTopics"
  val Init = "init"

  def bytes(str:String) = {
    try {
      str.getBytes("UTF-8")
    }catch {
      case e:UnsupportedEncodingException => throw new DolphinException(e)
    }
  }
}
