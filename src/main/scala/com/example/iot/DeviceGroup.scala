package com.example.iot

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import com.example.iot.DeviceManager.RequestTrackDevice



object DeviceGroup {
  def props(groupId: String): Props = Props(new DeviceGroup(groupId))
}

class DeviceGroup(groupId: String) extends Actor with ActorLogging {
  var deviceIdToActor = Map.empty[String, ActorRef]

  override def preStart(): Unit = log.info("device group {} started", groupId)
  override def postStop(): Unit = log.info("device group {} stopped", groupId)


  /**
    * in java
    * if (this.groupId.equals(trackMsg.groupId)) {
    * ActorRef deviceActor = deviceIdToActor.get(trackMsg.deviceId);
    * if (deviceActor != null) {
    *         deviceActor.forward(trackMsg, getContext());
    * } else {
    *         log.info("Creating device actor for {}", trackMsg.deviceId);
    * deviceActor =
    * getContext()
    * .actorOf(Device.props(groupId, trackMsg.deviceId), "device-" + trackMsg.deviceId);
    *         deviceIdToActor.put(trackMsg.deviceId, deviceActor);
    *         deviceActor.forward(trackMsg, getContext());
    * }
    * } else {
    *       log.warning(
    * "Ignoring TrackDevice request for {}. This actor is responsible for {}.",
    * groupId,
    *           this.groupId);
    * }
    *
    * @return
    */
  override def receive: Receive = {


    case RequestTrackDevice(requestId, groupId, deviceId) => {

      if (groupId == this.groupId) {

      }

    }

  }


}
