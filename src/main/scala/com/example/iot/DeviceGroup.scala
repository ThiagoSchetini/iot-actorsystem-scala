package com.example.iot

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import com.example.iot.DeviceManager.{RequestTrackDevice}

object DeviceGroup {
  def props(groupId: String): Props = Props(new DeviceGroup(groupId))
}

class DeviceGroup(groupId: String) extends Actor with ActorLogging {
  var deviceIdToActor = Map.empty[String, ActorRef]

  override def preStart(): Unit = log.info("device group {} started", groupId)
  override def postStop(): Unit = log.info("device group {} stopped", groupId)

  override def receive: Receive = {

    // the @ means to store the RequestTrackDevice on trackMsg reference
    case trackMsg @ RequestTrackDevice(_, `groupId`, deviceId) =>

      if (deviceIdToActor.get(deviceId).isEmpty) {
        log.info("creating device actor for {}", deviceId)
        val deviceActor = context.actorOf(Device.props(groupId, deviceId))
        deviceIdToActor += deviceId -> deviceActor
        deviceActor.forward(trackMsg)
      }
      else deviceIdToActor(deviceId).forward(trackMsg)

    case RequestTrackDevice(_, groupId, _) => log.warning(
        s"Ignoring TrackDevice Request for group {}. This actor is responsible for group {}",
      groupId, this.groupId)
  }
}
