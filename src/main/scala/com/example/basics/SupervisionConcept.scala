package com.example.basics

import akka.actor.{Actor, ActorSystem, Props}

object SupervisionConcept extends App {
  val system = ActorSystem.create("system")
  val supervisor = system.actorOf(SupervisingActor.props, "supervising-actor")
  supervisor ! "failChild"
}

object SupervisingActor {
  def props: Props = Props(new SupervisingActor)
}

class SupervisingActor extends Actor {
  val child = context.actorOf(SupervisedActor.props, "supervised-actor")
  override def receive: Receive = {
    case "failChild" =>
      println("supervised actor fails now")
      child ! "fail"
  }
}

object SupervisedActor {
  def props: Props = Props(new SupervisedActor)
}

class SupervisedActor extends Actor {
  override def preStart: Unit = println("supervised actor started")
  override def postStop: Unit = println("supervised actor stopped")

  override def receive: Receive = {
    case "fail" => throw new Exception("i failed")
  }
}
