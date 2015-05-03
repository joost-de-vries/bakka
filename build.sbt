name := """bakka"""

version := "1.0"

scalaVersion := "2.11.5"

//cache dependencies for faster builds. since 0.13.7
//see http://www.scala-sbt.org/0.13/docs/Cached-Resolution.html
updateOptions := updateOptions.value.withCachedResolution(cachedResoluton = true)

scalacOptions ++= Seq("-unchecked", "-feature", "-deprecation")

libraryDependencies ++= {
  val akkaStreamHttpVersion = "1.0-RC2"
  val akkaVersion="2.3.10"
  Seq(
    "com.typesafe.akka" %% "akka-stream-experimental" % akkaStreamHttpVersion,
    "com.typesafe.akka" %% "akka-http-scala-experimental" % akkaStreamHttpVersion,
    "com.typesafe.akka" %% "akka-http-core-experimental" % akkaStreamHttpVersion,
    "com.typesafe.akka" %% "akka-http-xml-experimental" % akkaStreamHttpVersion,
    "com.typesafe.akka" %% "akka-http-spray-json-experimental" % akkaStreamHttpVersion,
    "com.typesafe.akka" %% "akka-persistence-experimental" % akkaVersion,
    "com.typesafe.akka" %% "akka-actor" % akkaVersion,
    "org.scala-lang.modules" %% "scala-xml" % "1.0.4",

    //test dependencies
    "com.typesafe.akka" %% "akka-http-testkit-scala-experimental" % akkaStreamHttpVersion % "test",
    "com.typesafe.akka" % "akka-testkit_2.11" % akkaVersion % "test",
    "org.scalatest" %% "scalatest" % "2.2.4" % "test",
    "junit" % "junit" % "4.12" % "test",
    "org.scalacheck" %% "scalacheck" % "1.12.2" % "test",
    "commons-io" % "commons-io" % "2.4" % "test"
  )
}

