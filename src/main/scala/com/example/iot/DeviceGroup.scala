package com.example.iot

import akka.actor.{Actor, ActorLogging, ActorRef, Props, Terminated}
import com.example.iot.DeviceGroup.{ReplyDeviceList, RequestAllTemperatures, RequestDeviceList}
import com.example.iot.DeviceManager.RequestTrackDevice

import scala.concurrent.duration.FiniteDuration

object DeviceGroup {
  def props(groupId: String): Props = Props(new DeviceGroup(groupId))

  final case class RequestDeviceList(requestId: Long)
  final case class ReplyDeviceList(requestId: Long, ids:Set[String])

  final case class RequestAllTemperatures(requestId: Long, timeout: FiniteDuration)
  final case class ReplyAllTemperatures(requestId: Long, temperatures: Map[String, TemperatureReading])

  sealed trait TemperatureReading
  final case class Temperature(value: Double) extends TemperatureReading
  case object TemperatureNotAvailable extends TemperatureReading
  case object DeviceNotAvailable extends TemperatureReading
  case object DeviceTimedOut extends TemperatureReading
}

class DeviceGroup(groupId: String) extends Actor with ActorLogging {
  var deviceIdToActor = Map.empty[String, ActorRef]
  var actorToDeviceId = Map.empty[ActorRef, String]

  override def preStart(): Unit = log.info("device group {} started", groupId)
  override def postStop(): Unit = log.info("device group {} stopped", groupId)

  override def receive: Receive = {

    // the @ means to store the RequestTrackDevice on trackMsg reference
    case trackMsg @ RequestTrackDevice(_, `groupId`, _) =>

      deviceIdToActor.get(trackMsg.deviceId) match {

        case Some(deviceActor) => deviceActor.forward(trackMsg)

        case None =>
          log.info("creating device actor for {}", trackMsg.deviceId)
          val deviceActor = context.actorOf(Device.props(groupId, trackMsg.deviceId))
          deviceIdToActor += trackMsg.deviceId -> deviceActor
          actorToDeviceId += deviceActor -> trackMsg.deviceId
          context.watch(deviceActor)
          deviceActor.forward(trackMsg)
      }

    case RequestTrackDevice(_, groupId, _) => log.warning(
        s"Ignoring TrackDevice Request for group {}. This actor is responsible for group {}",
      groupId, this.groupId)

    case RequestDeviceList(requestId) => sender().tell(ReplyDeviceList(requestId, deviceIdToActor.keySet), self)

    case RequestAllTemperatures(requestId, timeout) =>
      context.actorOf(DeviceGroupQuery.props(actorToDeviceId, requestId, sender(), timeout))

    case Terminated(deviceActor) =>
      val deviceId = actorToDeviceId(deviceActor)
      log.info("Device actor for {} has been terminated", deviceId)
      actorToDeviceId -= deviceActor
      deviceIdToActor -= deviceId
  }
}
