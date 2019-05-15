package com.example.basics

import akka.actor.{Actor, ActorSystem, Props}

/**
  * the AKKA closes the child actors before the actor that is being stopped
  */
object ActorLifecycle extends App {
  val system = ActorSystem.create("system")
  val actor = system.actorOf(StartStopActor1.props)

  // ! means .tell()
  actor ! "stop"
}

object StartStopActor2 {
  def props: Props = Props(new StartStopActor2)
}

class StartStopActor2 extends Actor {

  override def preStart() {
    println("second started")
  }

  override def postStop(): Unit = {
    println("second stopped")
  }

  override def receive: Receive = Actor.emptyBehavior
}

object StartStopActor1 {
  def props: Props = Props(new StartStopActor1)
}

class StartStopActor1 extends Actor {

  override def preStart() {
    println("first started")
    context.actorOf(StartStopActor2.props, "second")
  }

  override def postStop(): Unit = println("first stopped")

  override def receive: Receive = {
    case "stop" => context.stop(self)
  }
}