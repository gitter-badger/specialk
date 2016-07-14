package com.biosimilarity.lift.lib

import org.scalatest.{WordSpec, Matchers}
import org.scalatest.concurrent.Eventually
import com.biosimilarity.lift.test._

class MonadicAMQPSpec extends WordSpec with Matchers with Eventually with AMQPTestUtility[String] {

  import com.biosimilarity.lift.lib.moniker.identityConversions._
  import java.net.URI

  import AMQPDefaults._

  override def msgStreamPayload(idx: Int): String = { "Msg" + idx }

  val localhost: URI = new URI("amqp", null, "localhost", 5672, "/mult", "routingKey=routeroute", null)

  trait Destination
  case object Src  extends Destination
  case object Trgt extends Destination

  def smjatp(d: Destination) = {
    val _smjatp = d match {
      case Src  => SMJATwistedPair[Message](localhost, localhost)
      case Trgt => SMJATwistedPair[Message](localhost, localhost)
    }
    _smjatp.jsonDispatcher { (msg: Message) =>
      println("received : " + msg)
    }
    val msgs = msgStream.take(100).toList
    _smjatp.jsonSender
    // Msg has been changed to Message to get this to compile, but...
    for (i <- 1 to 100) {
      _smjatp.send(msgs(i - 1))
    }
    _smjatp
  }

  "MonadicAMQP" should {
    "do something, but I don't know what" in {
      assert(true)
    }
  }
}
