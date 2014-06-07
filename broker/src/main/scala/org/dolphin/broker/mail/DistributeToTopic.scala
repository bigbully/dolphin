package org.dolphin.broker.mail

/**
 * User: bigbully
 * Date: 14-6-7
 * Time: 下午11:18
 */
case class DistributeToTopic(topic:String, subTopic:Option[String], data:Array[Byte]) {

}
