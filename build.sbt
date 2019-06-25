name := "iot-actorsystem-scala"

version := "1.0"

scalaVersion := "2.12.6"

lazy val akkaVersion = "2.5.19"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % akkaVersion,
  "com.typesafe.akka" %% "akka-testkit" % akkaVersion,
  "org.scalatest" %% "scalatest" % "3.0.5" % "test",

  //"org.slf4j" % "slf4j-api" % "1.7.5",
  "org.slf4j" % "slf4j-api" % "1.7.25",
  //"org.slf4j" % "slf4j-simple" % "1.7.5",
  "org.slf4j" % "slf4j-simple" % "1.7.25",
  "org.clapper" %% "grizzled-slf4j" % "1.3.2"
  
)
