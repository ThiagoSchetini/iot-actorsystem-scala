package com.example.interactionpatterns

import akka.actor.Actor
import akka.actor.typed.Behavior
import akka.actor.typed.javadsl.Behaviors
import akka.event.Logging


/***************************** we are using akka.actor.typed to build the patterns! ***********************************/


case class FireAndForget(message: String) extends Actor {

  val log = Logging(context.system, this)

  val fireBehavior: Behavior[FireAndForget] = Behaviors.receive {
    case (context, FireAndForget(message)) =>
      log.info(message)
      Behaviors.same
  }

  override def receive: Receive = ???
}


object FireAndForgetExecutor extends App {


}



