package com.example.basics.withactors

import akka.actor.{Actor, ActorSystem, Props}
import akka.event.Logging

object ActorChildAutoRestart extends App {
  val system = ActorSystem.create("system")
  val supervisor = system.actorOf(SupervisorActor.props, "supervising-actor")
  supervisor ! "failChild"
}

object SupervisorActor {
  def props: Props = Props(new SupervisorActor)
}

class SupervisorActor extends Actor {
  val log = Logging(context.system, this)
  val child = context.actorOf(SupervisedActor.props, "supervised-actor")

  override def preStart(): Unit = log.info("[SUPERVISOR] started")
  override def postStop(): Unit = log.info("[SUPERVISOR] stopped")

  override def receive: Receive = {
    case "failChild" =>
      log.info("supervised actor fails now")
      child ! "fail"
  }
}

object SupervisedActor {
  def props: Props = Props(new SupervisedActor)
}

class SupervisedActor extends Actor {
  val log = Logging(context.system, this)

  override def preStart(): Unit = log.info("[SUPERVISED] started")
  override def postStop(): Unit = log.info("[SUPERVISED] stopped")

  override def receive: Receive = {
    case "fail" => throw new Exception("i failed")
  }
}
