package com.biosimilarity.lift.model.store.mongo

import org.scalatest.{Matchers, WordSpec}

class MongoDBPersistUsage extends WordSpec with Matchers {

  import com.biosimilarity.lift.lib._
  import com.biosimilarity.lift.model.store.{CnxnCtxtLabel, JSONIfy}

  import scala.language.implicitConversions

  object MongoKVDBFeatureComponents
      extends MongoCnxnStorage[String, String, String]
      with JSONIfy[String, String, String]
      with MongoDBStore[String, String, String]
      with UUIDOps {

    object MyCCLConversions {

      def ids(s: String): String = s

      implicit def toConverter(ccl: CnxnCtxtLabel[String, String, String]): CCLConversionsWrapper =
        CCLConversionsWrapper(ccl, ids, ids, ids)

      def apply(ccl: CnxnCtxtLabel[String, String, String]): CCLConversionsWrapper =
        CCLConversionsWrapper(ccl, ids, ids, ids)
    }
  }

  object JSONData {

    import MongoKVDBFeatureComponents._

    val cclRcrd1: CnxnCtxtLabel[String, String, String] = CCLStringConversions(
      """record( key( t1( a( 1 ), b( "a string is born" ), c( true ) ) ), value( "stuff" ) )""").toCCL()

    val cclKRcrd1: CnxnCtxtLabel[String, String, String] = CCLStringConversions(
      """krecord( key( t1( a( 1 ), b( "a string is born" ), c( true ) ) ), value( "<ASerializedClosure>" ) )""").toCCL()
  }

  "MonadicDBPersist" should {
    "do something" in {
      assert(true)
    }
  }
}
