package com.biosimilarity.lift.model.store

import org.scalatest.{Matchers, WordSpec}

/**
  * Created by ht on 7/12/16.
  */
class MonadicKVDBNodeSpec extends WordSpec with Matchers {

  import MonadicKVDBNodeSetup.mTT
  import com.biosimilarity.lift.lib.BasicLogService

  object TestHelpers {

    import scala.collection.mutable

    type UMap = mutable.LinkedHashMap[String, CnxnCtxtLabel[String, String, String]]

    def simpleGet(getKey: String): List[String] =
      MonadicKVDBNodeInstance.returnGet(getKey).collect {
        case Some(mTT.Ground(x)) => x
      }

    def getWithQuery(getKey: String): List[(String, UMap)] =
      MonadicKVDBNodeInstance.returnGet(getKey).collect {
        case Some(mTT.RBoundHM(Some(mTT.Ground(x)), Some(y))) => (x, y)
      }

    def roundTrip(key: String, value: String): List[String] = {
      MonadicKVDBNodeInstance.doPut(key, value)
      simpleGet(key)
    }

    def roundTripWithQuery(putKey: String, value: String, getKey: String): List[(String, UMap)] = {
      MonadicKVDBNodeInstance.doPut(putKey, value)
      getWithQuery(getKey)
    }

    def translateUnifications(xs: List[UMap]): List[mutable.LinkedHashMap[String, String]] = xs.map { (x: UMap) =>
      x.map { case (k, v) => k -> v.toString }
    }

    def unificationsContainBinding(results: List[UMap], binding: (String, String)): Boolean =
      translateUnifications(results).exists { (m: mutable.LinkedHashMap[String, String]) =>
        m.exists(_ == binding)
      }
  }

  "An instance of MonadicKVDBNode" should {

    "let us put a value into the database" in {
      val key: String                = """terminatorSeries(jday(characters(name(first("Sarah"), last("Connor")))))"""
      val value: String              = "They lived only to face a new nightmare: the war against the machines"
      MonadicKVDBNodeInstance.doPut(key, value) should equal(())
    }

    "let us put a value into the database with a given key and get back the same value using that key (1)" in {
      val key: String                = """terminatorSeries(jday(characters(name(first("Sarah"), last("Connor")))))"""
      val value: String              = "They lived only to face a new nightmare: the war against the machines"
      val returnValues: List[String] = TestHelpers.roundTrip(key, value)
      returnValues should contain(value)
    }

    "let us put a value into the database with a given key and get back the same value using that key (2)" in {
      val key: String                = """terminatorSeries(jday(characters(name(first("John"), last("Connor")))))"""
      val value: String              = "Chill out"
      val returnValues: List[String] = TestHelpers.roundTrip(key, value)
      returnValues should contain(value)
    }

    "return List(None) when no value exists for a given key" in {
      val key: String                              = """terminatorSeries(jday(characters(name(first("Sarah"), last("Connor")))))"""
      val returnValues: List[Option[mTT.Resource]] = MonadicKVDBNodeInstance.returnGet(key)
      returnValues should equal(List[Option[mTT.Resource]](None))
    }

    "return List(None) when no value exists for a given key (2)" in {
      val putKey: String   = """terminatorSeries(jday(characters(name(first("John"), last("Connor")))))"""
      val putValue: String = "Chill out"
      val getKey: String   = """terminatorSeries(jday(characters(name(first("Sarah"), last("Connor")))))"""

      MonadicKVDBNodeInstance.doPut(putKey, putValue)
      val returnValues: List[Option[mTT.Resource]] = MonadicKVDBNodeInstance.returnGet(getKey)
      returnValues should equal(List[Option[mTT.Resource]](None))
    }

    """|let us put a value into the database with a given key
      |and get back the same value using a Prolog query against that key""".stripMargin in {
      val unifyWithThis: String = "Connor"
      val prologVar: String     = "X"
      val putKey: String        = s"""terminatorSeries(jday(characters(name(first("John"), last("$unifyWithThis")))))"""
      val getKey: String        = s"""terminatorSeries(jday(characters(name(first("John"), last($prologVar)))))"""
      val value: String         = "No problemo"

      val (rValues, rUnifications): (List[String], List[TestHelpers.UMap]) = TestHelpers.roundTripWithQuery(putKey, value, getKey).unzip

      BasicLogService.tweet(s"Unifications: $rUnifications")

      TestHelpers.unificationsContainBinding(rUnifications, prologVar -> unifyWithThis) should be(true)
      rValues should contain(value)
    }

    /*
     * """|let us put two values into the database with related keys
     *   |and get back both values using a Prolog query against their keys""".stripMargin in {
     *   val prologVar: String  = "X"
     *   val sarahKey: String   = """terminatorSeries(jday(characters(name(first("Sarah"), last("Connor")))))"""
     *   val sarahValue: String = "You came here to stop me?"
     *   val johnKey: String    = """terminatorSeries(jday(characters(name(first("John"), last("Connor")))))"""
     *   val johnValue: String  = "Yeah, I did."
     *
     *   MonadicKVDBNodeInstance.doPut(sarahKey, sarahValue)
     *   MonadicKVDBNodeInstance.doPut(johnKey, johnValue)
     *
     *   val getKey: String = s"""terminatorSeries(jday(characters(name(first($prologVar), last("Connor")))))"""
     *
     *   val (rValues, rUnifications): (List[String], List[TestHelpers.UMap]) = TestHelpers.getWithQuery(getKey).unzip
     *
     *   BasicLogService.tweet(s"Unifications: $rUnifications")
     *
     *   TestHelpers.unificationsContainBinding(rUnifications, prologVar -> "Sarah") should be(true)
     *   TestHelpers.unificationsContainBinding(rUnifications, prologVar -> "John") should be(true)
     *   rValues should contain(sarahValue)
     *   rValues should contain(johnValue)
     * }
     *
     * """|let us put two values into the database with related keys
     *   |and get back nothing using a Prolog query against their keys which shouldn't have any matches""".stripMargin in {
     *   val prologVar: String  = "X"
     *   val sarahKey: String   = """terminatorSeries(jday(characters(name(first("Sarah"), last("Connor")))))"""
     *   val sarahValue: String = "You came here to stop me?"
     *   val johnKey: String    = """terminatorSeries(jday(characters(name(first("John"), last("Connor")))))"""
     *   val johnValue: String  = "Yeah, I did."
     *
     *   MonadicKVDBNodeInstance.doPut(sarahKey, sarahValue)
     *   MonadicKVDBNodeInstance.doPut(johnKey, johnValue)
     *
     *   val getKey: String = s"""terminatorSeries(jday(characters(name(first($prologVar), last("Reese")))))"""
     *
     *   val (rValues, rUnifications): (List[String], List[TestHelpers.UMap]) = TestHelpers.getWithQuery(getKey).unzip
     *
     *   BasicLogService.tweet(s"Unifications: $rUnifications")
     *
     *   rUnifications should equal(List[TestHelpers.UMap]())
     *   rValues should equal(List[String]())
     * }
     */

    /*
     * """|let us put a values into the database with a Prolog query as a key
     *    |and get back the value using a matching Prolog term""".stripMargin in {
     *   TestKVDB.mc1.drop()
     *   val prologVar: String = "X"
     *   val putKey: String    = s"""terminatorSeries(jday(characters(name(first($prologVar), last("Connor")))))"""
     *   val putValue: String  = "Chill out"
     *
     *   TestKVDB.doPut(putKey, putValue)
     *
     *   val getKey: String = s"""terminatorSeries(jday(characters(name(first("John), last("Connor")))))"""
     *
     *   val r = TestKVDB.returnGet(getKey)
     *
     *   BasicLogService.tweet(s"r is: $r")
     *
     *   // val (rValues, rUnifications): (List[String], List[TestHelpers.UMap]) = TestHelpers.getWithQuery(getKey).unzip
     *
     *   // BasicLogService.tweet(s"Unifications: $rUnifications")
     *
     *   // rUnifications should equal(List[TestHelpers.UMap]())
     *   // rValues should equal(List[String]())
     *   assert(true)
     * }
     */
  }
}
