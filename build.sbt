import AssemblyKeys._

organization  := "com.synereo"

name := "specialk"

version       := "0.1"

scalaVersion  := "2.10.2"

autoCompilerPlugins := true

scalacOptions := Seq("-unchecked", "-deprecation", "-encoding", "utf8",
  "-P:continuations:enable")

resolvers ++= Seq(
  "local-maven-cache repo" at "file://" + Path.userHome.absolutePath + "/.m2/repository/",
  "sonatype.repo" at "https://oss.sonatype.org/content/repositories/public/",      
  "Java.net Maven2 Repository" at "http://download.java.net/maven/2/",
  "modafocas" at "http://repo.modafocas.org/nexus/content/repositories/modafocas-release/",
  "apache.snapshots" at "http://repository.apache.org/snapshots/",
  "repository.codehaus.org" at "http://repository.codehaus.org/com/thoughtworks",
  "milton" at "http://milton.io/maven/com/ettrema/milton",
  "biosim repo" at "http://biosimrepomirror.googlecode.com/svn/trunk/"
)   

publishTo := Some(Resolver.file("file",  new File(Path.userHome.absolutePath+"/.m2/repository")))

libraryDependencies ++= Seq(
  "org.scala-lang"         %   "scala-actors"       % "2.10.2",
  "org.scala-lang"         %   "scala-reflect"      % "2.10.2",
  "javax.persistence" % "persistence-api" % "1.0" % "provided",
  "javax.servlet" % "servlet-api" % "2.5" % "provided",
  "org.coconut.forkjoin" % "jsr166y" % "070108",
  "jlex" % "JLex-local" % "local",
  "cup" % "java-cup-11a" % "local",
  "cup" % "java-cup-11a-runtime" % "local",
  "com.rabbitmq" % "amqp-client" % "3.5.3",      
  "com.thoughtworks.xstream" % "xstream" % "1.4.4",
  "com.typesafe" % "config" % "1.0.0",
  "ch.qos.logback" % "logback-classic" % "0.9.26",      
  "ch.qos.logback" % "logback-core" % "0.9.26",
  "log4j" % "log4j" % "1.2.17",
  "org.prolog4j" % "prolog4j-api" % "0.2.1-SNAPSHOT",
  "it.unibo.alice.tuprolog" %  "tuprolog" % "2.1.1",
  "org.mongodb"            %   "casbah_2.10"       % "2.5.1",
  "biz.source_code"        %   "base64coder"        % "2010-09-21",
  "org.basex"              %   "basex-api"          % "7.5",
  "org.json4s" % "json4s-jackson_2.10" % "3.2.4",
  "org.apache.commons" % "commons-io" % "1.3.2",
  "org.apache.ws.commons.util" % "ws-commons-util" % "1.0.2",
  "commons-pool" % "commons-pool" % "1.6",
  "org.scalesxml" % "scales-xml_2.10" % "0.4.5",
  //"org.scalesxml" % "scales-jaxen_2.10" % "0.4.5",
  "junit" % "junit" % "4.10" % "test",      
//  "org.scalatest" % "scalatest_2.10" % "2.2.5" % "test",
  compilerPlugin("org.scala-lang.plugins" % "continuations" % "2.10.2")
)

//seq(Revolver.settings: _*)

sbtassembly.Plugin.assemblySettings

test in assembly := {}

mergeStrategy in assembly <<= (mergeStrategy in assembly) { (old) =>
  {
    case PathList("org", "fusesource", "jansi", xs @ _*) => MergeStrategy.first
    case PathList("META-INF", "native", "osx", "libjansi.jnilib") => MergeStrategy.first
    case PathList("META-INF", "ECLIPSEF.RSA") => MergeStrategy.last
    case "plugin.properties" => MergeStrategy.last
    case "about.html" => MergeStrategy.discard
    case x => old(x)
  }
}

net.virtualvoid.sbt.graph.Plugin.graphSettings
