package org.dolphin.client

import org.dolphin.Util.Waiting

/**
 * User: bigbully
 * Date: 14-4-26
 * Time: 下午9:47
 */
object ClientTest {
  implicit val waiting = Waiting(3)

  def main(args: Array[String]) {
    val producer = ClientFactory.createProducer(ClientConfig("127.0.0.1", 11111, ("send.strategy", "round-robin")))
    val topic = "myTopic"
    val cluster = "myCluster"
    producer.publish(topic, cluster)
    producer.send("123".getBytes)
  }

}
