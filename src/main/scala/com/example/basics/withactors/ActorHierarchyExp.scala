package com.example.basics.withactors

import akka.actor.{Actor, ActorSystem, Props}
import scala.io.StdIn

object PrinterActor {
  def props: Props = Props(new PrinterActor)
}

class PrinterActor extends Actor {
  override def receive: Receive = {
    case "printit" =>
      val secondRef = context.actorOf(Props.empty, "second-actor")

      // testSystem/user/first-actor/second-actor
      println(s"Second: $secondRef")
  }
}

object ActorHierarchyExp extends App {
  val system = ActorSystem("testSystem")

  val firstRef = system.actorOf(PrinterActor.props, "first-actor")

  // testSystem/user/first-actor
  println(s"First: $firstRef")

  firstRef ! "printit"

  println(">>> Press ENTER to exit <<<")
  try StdIn.readLine()
  finally system.terminate()
}