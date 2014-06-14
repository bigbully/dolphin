package org.dolphin.domain

/**
 * User: bigbully
 * Date: 14-5-25
 * Time: 上午1:32
 */
case class Message(content :Array[Byte], topic:Array[Byte] = null, subTopic:Option[Array[Byte]] = null, sync:Boolean = false) {

}
