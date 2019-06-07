package com.example.iot

import akka.actor.{Actor, ActorLogging, ActorRef, Props, Terminated}
import com.example.iot.DeviceManager.RequestTrackDevice

object DeviceManager {
  def props(): Props = Props(new DeviceManager)

  case class RequestTrackDevice(requestId: Long, groupId: String, deviceId: String)
  case class DeviceRegistered(requestId: Long)
}

class DeviceManager extends Actor with ActorLogging {
  var groupIdToActor = Map.empty[String, ActorRef]
  var actorToGroupId = Map.empty[ActorRef, String]

  override def preStart() = log.info("a group manager started")
  override def postStop() = log.info("a group manager stopped")

  override def receive: Receive = {
    case trackMsg @ RequestTrackDevice(_, groupId, _) =>
      groupIdToActor.get(groupId) match {
        case Some(groupActor) =>
          groupActor.forward(trackMsg)
        case None =>
          val groupActor = context.actorOf(DeviceGroup.props(trackMsg.groupId))
          context.watch(groupActor)
          groupIdToActor += groupId -> groupActor
          actorToGroupId += groupActor -> groupId
          log.info("creating device group actor for group {}", trackMsg.groupId)
          groupActor.forward(trackMsg)
      }

    case Terminated(groupActor) =>
      val groupId = actorToGroupId(groupActor)
      groupIdToActor -= groupId
      actorToGroupId -= groupActor
      log.info("device group actor for group {} has been terminated", groupId)
  }
}
