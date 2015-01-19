name := """bakka"""

version := "1.0"

scalaVersion := "2.11.4"

//cache dependencies for faster builds. since 0.13.7
//see http://www.scala-sbt.org/0.13/docs/Cached-Resolution.html
updateOptions := updateOptions.value.withCachedResolution(cachedResoluton = true)

libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "2.1.6" % "test",
  "junit" % "junit" % "4.12" % "test",
  "org.scalacheck" %% "scalacheck" % "1.12.1" % "test",
  "com.typesafe.akka" % "akka-stream-experimental_2.11" % "1.0-M2",
  "com.typesafe.akka" % "akka-http-experimental_2.11" % "1.0-M2",
  "com.typesafe.akka" % "akka-http-core-experimental_2.11" % "1.0-M2",
  "com.typesafe.akka" % "akka-http-xml-experimental_2.11" % "1.0-M2",
  "com.typesafe.akka" % "akka-http-spray-json-experimental_2.11" % "1.0-M2",
  "org.scala-lang.modules" %% "scala-xml" % "1.0.2",
  "com.typesafe.akka" % "akka-testkit_2.11" % "2.3.7" % "test"
)

