name := """bakka"""

version := "1.0"

scalaVersion := "2.11.5"

//cache dependencies for faster builds. since 0.13.7
//see http://www.scala-sbt.org/0.13/docs/Cached-Resolution.html
updateOptions := updateOptions.value.withCachedResolution(cachedResoluton = true)

scalacOptions ++= Seq("-unchecked", "-feature", "-deprecation")

libraryDependencies ++= {
  val akkaStreamHttpVersion = "1.0-M3"
  val akkaVersion="2.3.9"
  Seq(
    "com.typesafe.akka" % "akka-stream-experimental_2.11" % akkaStreamHttpVersion,
    "com.typesafe.akka" % "akka-http-experimental_2.11" % akkaStreamHttpVersion,
    "com.typesafe.akka" % "akka-http-core-experimental_2.11" % akkaStreamHttpVersion,
    "com.typesafe.akka" % "akka-http-xml-experimental_2.11" % akkaStreamHttpVersion,
    "com.typesafe.akka" % "akka-http-spray-json-experimental_2.11" % akkaStreamHttpVersion,
    "com.typesafe.akka" % "akka-persistence-experimental_2.11" % akkaVersion,
    "org.scala-lang.modules" %% "scala-xml" % "1.0.2",

    //test dependencies
    "com.typesafe.akka" %% "akka-http-testkit-experimental" % akkaStreamHttpVersion % "test",
    "com.typesafe.akka" % "akka-testkit_2.11" % akkaVersion % "test",
    "org.scalatest" %% "scalatest" % "2.1.6" % "test",
    "junit" % "junit" % "4.12" % "test",
    "org.scalacheck" %% "scalacheck" % "1.12.1" % "test",
    "commons-io" % "commons-io" % "2.4" % "test"
  )
}

