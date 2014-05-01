package org.dolphin.domain

/**
 * User: bigbully
 * Date: 14-4-26
 * Time: 下午6:33
 */
case class BrokerModel(id:Int, host:String, port:Int, cluster:String, topics:Option[List[String]]) extends Model{
  val path = "akka.tcp://broker@"+host+":"+port+"/user/" + id
}
