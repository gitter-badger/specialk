// -*- mode: Scala;-*- 
// Filename:    WebSocket.scala 
// Authors:     lgm                                                    
// Creation:    Fri Dec  2 14:28:24 2011 
// Copyright:   Not supplied 
// Description: 
// ------------------------------------------------------------------------

package com.biosimilarity.lift.lib.websocket

import com.biosimilarity.lift.lib.moniker._
import com.biosimilarity.lift.lib.UUIDOps

import net.liftweb.amqp._

import scala.collection.mutable.Queue
import scala.collection.mutable.HashMap
import scala.collection.mutable.MapProxy
import scala.collection.SeqProxy
import scala.util.continuations._

import scala.concurrent.{Channel => Chan, _}
import scala.concurrent.cpsops._

import _root_.com.rabbitmq.client.{ Channel => RabbitChan, _}
import _root_.scala.actors.Actor

import org.eclipse.jetty.websocket.WebSocket

import com.thoughtworks.xstream.XStream
import com.thoughtworks.xstream.io.json.JettisonMappedXmlDriver

import org.apache.http.HttpResponse
import org.apache.http.HttpEntity
import org.apache.http.impl.nio.client.DefaultHttpAsyncClient
import org.apache.http.client.methods.HttpGet
import org.apache.http.nio.conn.ClientConnectionManager
//import org.apache.http.nio.client.methods.BaseHttpAsyncRequestProducer
import org.apache.http.nio.client.HttpAsyncClient
import org.apache.http.nio.concurrent.FutureCallback

import java.io.IOException;
import java.util.concurrent.Future;

import java.net.URI
import java.util.UUID
import _root_.java.io.ObjectInputStream
import _root_.java.io.ByteArrayInputStream
import _root_.java.util.Timer
import _root_.java.util.TimerTask
import java.net.URI
import com.biosimilarity.lift.lib.kvdbJSON.KVDBJSONAPIDispatcher

trait SocketConnectionPair {
  def webSocket : WebSocket with Seq[String]
  def responseConnection : WebSocket.Connection
}

case class SCP(
  webSocket : WebSocket with Seq[String]
  , responseConnection : WebSocket.Connection
) extends SocketConnectionPair

case class QueuingWebSocket( 
  requestQueue : Queue[String]
  , openCB : SocketConnectionPair => Unit
  , closeCB : () => Unit
) extends WebSocket 
     with WebSocket.OnTextMessage
     with SeqProxy[String]
{ 
  override def self = requestQueue
  override def onOpen(
    wsConnection: WebSocket.Connection
  ) : Unit = {
    println( "in onOpen with " + wsConnection )
    openCB( SCP( this, wsConnection ) )    
  }
  
  override def onClose(
    closeCode: Int,
    message: String
  ) : Unit = {
    println( "in onClose with " + closeCode + " and " + message )
    closeCB()
  }
  
  override def onMessage(
    message: String
  ) : Unit = {
    // is this thread safe?
    println("adding message to queue (of size " + requestQueue.size + ")  " + message)
    requestQueue += message
  }    
}

