package com.example.iot

import akka.actor.{Actor, ActorLogging, Props}


object IotSupervisor {
  def props: Props = Props(new IotSupervisor)
}


class IotSupervisor extends Actor with ActorLogging {
  override def preStart(): Unit = log.info("IotSupervisor started")
  override def postStop(): Unit = log.info("IotSupervisor stopped")
  override def receive(): Receive = Actor.emptyBehavior
}