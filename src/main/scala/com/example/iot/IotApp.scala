package com.example.iot

import akka.actor.ActorSystem

import scala.io.StdIn

object IotApp extends App {

  val actorSystem= ActorSystem.create("iot-system")
  val iotSupervisor = actorSystem.actorOf(IotSupervisor.props, "iot-supervisor")

  println("press enter twice to exit")

  try StdIn.readLine()
  finally actorSystem.terminate()

}
