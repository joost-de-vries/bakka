name := """bank-test"""

version := "1.0"

scalaVersion := "2.11.4"

libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "2.1.6" % "test",
  "junit" % "junit" % "4.12" % "test",
  "org.scalacheck" %% "scalacheck" % "1.12.1" % "test",
  "com.typesafe.akka" % "akka-stream-experimental_2.11" % "1.0-M2",
  "com.typesafe.akka" % "akka-http-experimental_2.11" % "1.0-M2",
  "com.typesafe.akka" % "akka-http-core-experimental_2.11" % "1.0-M2",
  "com.typesafe.akka" % "akka-testkit_2.11" % "2.3.7" % "test"
)

