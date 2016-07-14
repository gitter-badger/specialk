package com.biosimilarity.lift.model.store

import org.scalatest.{Matchers, WordSpec}

class CnxnMongoSpec extends WordSpec with Matchers {

  import com.biosimilarity.lift.model.store.CnxnMongoSetup._
  import com.mongodb.casbah.Imports.DBObject
  import org.json4s.JValue
  import org.json4s.jackson.JsonMethods.parse

  import scala.language.implicitConversions

  def ids(s: String): String = s

  object TestHelpers {

    def unify(termA: String, termB: String): Option[CnxnCtxtLabel[String, String, String] with Factual] = {
      val ccl1: CnxnCtxtLabel[String, String, String] = CCLStringConversions(termA).toCCL()
      val ccl2: CnxnCtxtLabel[String, String, String] = CCLStringConversions(termB).toCCL()
      val mdbo1: DBObject                             = MyCCLConversions(ccl1).toMongoObject()

      CnxnMongoQuerifier()
        .queryBindings(ccl2, mdbo1)(identity, identity, identity)
        .headOption
        .map(_._2)
        .map { (y: DBObject) => parse(y.toString) }
        .map { (z: JValue) => CnxnMongoObjectifier().fromJSON(z) }
    }
  }

  "A CnxnMongoQuerifier" should {

    "perform unification (1)" in {

      val termA: String = """t1(a(1), b("a string is born"), c(true))"""
      val termB: String = """t1(a(1), b(X), c(true) )"""

      val result: Option[String] = TestHelpers.unify(termA, termB).map { (c: CnxnCtxtLabel[String, String, String]) =>
        c.toString
      }

      result should equal(Some("a string is born"))
    }
  }
}
