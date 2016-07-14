package com.biosimilarity.lift.lib

import org.scalatest.concurrent.Eventually
import org.scalatest.time.{Millis, Seconds, Span}
import org.scalatest.{Matchers, WordSpec}

class AMQPMndSpec extends WordSpec with Matchers with Eventually {

  import scala.collection.mutable

  implicit override val patienceConfig =
    PatienceConfig(timeout = scaled(Span(5, Seconds)), interval = scaled(Span(15, Millis)))

  def roundTrip[A](src: String,
                   trgt: String,
                   qName: String,
                   sendSet: mutable.Set[A],
                   receiveSet: mutable.Set[A]): Unit = {
    val srcScope  = new AMQPStdScope[A]()
    val trgtScope = new AMQPStdScope[A]()
    val srcQM     = new srcScope.AMQPQueueHostExchangeM[A](src, qName)
    val trgtQM    = new trgtScope.AMQPQueueHostExchangeM[A](trgt, qName)
    val srcQ      = srcQM.zero[A]
    val trgtQ     = trgtQM.zero[A]
    for (msg <- trgtQM(trgtQ)) {
      receiveSet += msg
    }
    for (v <- sendSet) {
      srcQ ! v
    }
  }

  "A set of integers sent over AMQP" should {
    "contain the same elements as the set of integers which are eventually received" in {

      val sendSet: mutable.Set[Int]    = mutable.Set(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
      val receiveSet: mutable.Set[Int] = mutable.Set()
      val qName: String                = "AMQPMndUsage"

      roundTrip[Int]("localhost", "localhost", qName, sendSet, receiveSet)

      eventually { receiveSet should contain theSameElementsAs sendSet }
    }
  }
}
