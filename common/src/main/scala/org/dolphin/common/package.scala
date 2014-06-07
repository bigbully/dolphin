package org.dolphin

/**
 * User: bigbully
 * Date: 14-5-1
 * Time: 下午8:53
 */
package object common {

  val UTF_8 = "utf-8"
  val PRODUCER = 0
  val CONSUMER = 1
  val DEFAULT_CLUSTER = "default"
  val REGISTRY_ACT_NAME = "registry"
  val ENROLL_ACT_NAME = "enrollAct"
  val STORE_ACT_NAME = "storeAct"
  val REGISTER_SUCCESS = "registerSuccess"


  def generateStrId(id:Int) = {
    ("0" * (10 - id.toString.length)) + id
  }
}
