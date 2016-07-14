package com.biosimilarity.lift.model.store

import com.biosimilarity.lift.lib.BasicLogService
import com.biosimilarity.lift.model.store.PersistedMonadicKVDBMongoNodeSetup._
import com.mongodb.casbah.Imports.{MongoClient, MongoCollection}

import scala.collection.mutable
import scala.util.continuations._

/**
  * Created by ht on 7/8/16.
  */
object PersistedMonadicKVDBMongoNodeInstance extends CnxnString[String, String, String] with Serializable {

  val node1: BUMPKIN[PNReq, PNRsp] = setup[PNReq, PNRsp]("localhost", 5672, "localhost", 5672) match {
    case Left(n) => n
    case _       => throw new Exception("!")
  }

  val pd1: Being.MongoDBManifest = node1.cache.persistenceManifest.getOrElse(throw new Exception("!")).asInstanceOf[Being.MongoDBManifest]
  val clntSess1: MongoClient     = node1.cache.client
  val mc1: MongoCollection       = clntSess1.getDB(node1.cache.defaultDB)(pd1.storeUnitStr)

  def doGet(termStr: String) =
    for (term <- fromTermString(termStr)) {
      reset {
        for (e <- node1.get(term)) { BasicLogService.tweet(e) }
      }
    }

  def doGetAlt(termStr: String): Option[List[Option[mTT.Resource]]] =
    fromTermString(termStr).map { (term: CnxnCtxtLabel[String, String, String]) =>
      reset(node1.get(term))
    }

  def returnGet(termStr: String): List[Option[mTT.Resource]] = {
    val returnValue: mutable.ListBuffer[Option[mTT.Resource]] = mutable.ListBuffer()
    for (term <- fromTermString(termStr)) {
      reset {
        for (e <- node1.get(term)) {
          returnValue += e
          BasicLogService.tweet(s"""|Adding to return value
                                    |for key: $termStr:
                                    |--------------------
                                    |value: $e""".stripMargin)
        }
      }
    }
    BasicLogService.tweet(s"""|Returning value
                              |for key: $termStr
                              |--------------------
                              |value: $returnValue""".stripMargin)
    returnValue.toList
  }

  def doPut(termStr: String, value: String) =
    for (term <- fromTermString(termStr)) {
      reset { node1.put(term, value) }
    }

  def doSubscribe(termStr: String) =
    for (term <- fromTermString(termStr)) {
      reset {
        for (e <- node1.subscribe(term)) { BasicLogService.tweet(e) }
      }
    }

  def doPublish(termStr: String, value: String) =
    for (term <- fromTermString(termStr)) {
      reset { node1.publish(term, value) }
    }
}
