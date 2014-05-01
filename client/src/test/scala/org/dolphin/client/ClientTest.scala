package org.dolphin.client

/**
 * User: bigbully
 * Date: 14-4-26
 * Time: 下午9:47
 */
object ClientTest {

  def main(args:Array[String]){
    val producer = ClientFactory.createProducer(ClientConfig("127.0.0.1", 11111))
    val topic = "myTopic"
    val cluster = "myCluster"
    producer.publish(topic, cluster)
  }

}
