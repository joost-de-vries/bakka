name := """bakka"""

version := "1.0"

scalaVersion := "2.11.6"


//cache dependencies for faster builds. since 0.13.7
//see http://www.scala-sbt.org/0.13/docs/Cached-Resolution.html
updateOptions := updateOptions.value.withCachedResolution(cachedResoluton = true)
//faster incremental compilation
incOptions := incOptions.value.withNameHashing(true)

scalacOptions ++= Seq("-unchecked", "-feature", "-deprecation", "-Xlint:_", "-Ybackend:GenBCode", "-encoding", "UTF8")

libraryDependencies ++= {
  val akkaStreamHttpVersion = "1.0-RC3"
  val akkaVersion = "2.3.11"
  Seq(
    "com.typesafe.akka" %% "akka-stream-experimental" % akkaStreamHttpVersion,
    "com.typesafe.akka" %% "akka-http-experimental" % akkaStreamHttpVersion,
    "com.typesafe.akka" %% "akka-http-xml-experimental" % akkaStreamHttpVersion,
    "com.typesafe.akka" %% "akka-http-spray-json-experimental" % akkaStreamHttpVersion,
    "com.typesafe.akka" %% "akka-persistence-experimental" % akkaVersion,
    "com.typesafe.akka" %% "akka-actor" % akkaVersion,
    "org.scala-lang.modules" %% "scala-xml" % "1.0.4",

    //test dependencies
    "com.typesafe.akka" %% "akka-http-testkit-experimental" % akkaStreamHttpVersion % "test",
    "com.typesafe.akka" % "akka-testkit_2.11" % akkaVersion % "test",
    "org.scalatest" %% "scalatest" % "2.2.5" % "test",
    "junit" % "junit" % "4.12" % "test",
    "org.scalacheck" %% "scalacheck" % "1.12.2" % "test",
    "commons-io" % "commons-io" % "2.4" % "test"
  )
}

