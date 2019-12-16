name := "iot-actorsystem-scala"

version := "1.0"
scalaVersion := "2.12.8"
lazy val akkaVersion = "2.5.23"

libraryDependencies ++= Seq(

  // AKKA
  "com.typesafe.akka" %% "akka-actor" % akkaVersion,
  "com.typesafe.akka" %% "akka-actor-typed" % akkaVersion,
  "com.typesafe.akka" %% "akka-testkit" % akkaVersion,
  
  // tests
  "org.scalatest" %% "scalatest" % "3.0.5" % "test",

  // scala encapsulating Java logs
  "org.slf4j" % "slf4j-api" % "1.7.25",
  "org.slf4j" % "slf4j-simple" % "1.7.25",
  "org.clapper" %% "grizzled-slf4j" % "1.3.2"
)
