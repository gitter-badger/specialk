package com.biosimilarity.lift.lib

import org.scalatest.concurrent.Eventually
import org.scalatest.time.{Millis, Seconds, Span}
import org.scalatest.{Matchers, WordSpec}

class AMQPTwistedPairMndSpec extends WordSpec with Matchers with Eventually {

  import java.net.URI

  import scala.collection.mutable

  implicit override val patienceConfig =
    PatienceConfig(timeout = scaled(Span(5, Seconds)), interval = scaled(Span(15, Millis)))

  def roundTrip[A](src: URI,
                   trgt: URI,
                   qName: String,
                   sendSet: mutable.Set[A],
                   receiveSet: mutable.Set[A]): Unit = {
    val scope: AMQPTwistedPairScope[A]
        with AMQPBrokerScope[A]
        with MonadicDispatcherScope[A] = AMQPStdTPS[A](src, trgt)
    val qpM: scope.AMQPQueueMQT[A,scope.AMQPAbstractQueue] =
      new scope.TwistedQueuePairM[A](qName, "routeroute")
        .asInstanceOf[scope.AMQPQueueMQT[A,scope.AMQPAbstractQueue]]
    val qtp: scope.AMQPAbstractQueue[A] = qpM.zero[A]
    for (msg <- qpM(qtp)) {
      receiveSet += msg
    }
    for (v <- sendSet) {
      qtp ! v
    }
  }

  "A set of integers sent over AMQPTwistedPair" should {
    "contain the same elements as the set of integers which are eventually received" in {

      val sendSet: mutable.Set[Int]    = mutable.Set(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
      val receiveSet: mutable.Set[Int] = mutable.Set()
      val qName: String        = "AMQPTwistedPairMndUsage"

      val localhost: URI =
        new URI("amqp", null, "localhost", 5672, "/mult", "routingKey=routeroute", null)

      roundTrip[Int](localhost, localhost, qName, sendSet, receiveSet)

      eventually { receiveSet should contain theSameElementsAs sendSet }
    }
  }
}
