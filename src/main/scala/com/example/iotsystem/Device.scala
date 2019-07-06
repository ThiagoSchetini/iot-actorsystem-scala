package com.example.iotsystem

import akka.actor.{Actor, ActorLogging, Props}
import com.example.iotsystem.Device.{ReadTemperature, RecordTemperature, RespondTemperature, TemperatureRecorded}
import com.example.iotsystem.DeviceManager.DeviceRegistered

object Device {
  def props(groupId: String, deviceId: String): Props = Props(new Device(groupId, deviceId))

  final case class ReadTemperature(requestId: Long)
  final case class RespondTemperature(requestId: Long, value: Option[Double])

  final case class RecordTemperature(requestId: Long, value: Double)
  final case class TemperatureRecorded(requestId: Long)
}

class Device(groupId: String, deviceId: String) extends Actor with ActorLogging {

  var lastTemperatureReading: Option[Double] = None

  override def preStart(): Unit = log.info("device actor {}-{} started", groupId, deviceId)
  override def postStop(): Unit = log.info("device actor {}-{} stopped", groupId, deviceId)

  override def receive: Receive = {
    case DeviceManager.RequestTrackDevice(requestId, groupId, deviceId) =>
      if (groupId == this.groupId && deviceId == this.deviceId) {
        sender.tell(DeviceRegistered(requestId), self)
      } else {
        log.warning(
          s"Ignoring track request $requestId for group {} and device {}. This actor responsible for group {} device {}",
          groupId, deviceId, this.groupId, this.deviceId
        )
      }

    case ReadTemperature(requestId) => sender.tell(RespondTemperature(requestId, lastTemperatureReading), self)

    case RecordTemperature(requestId, value) => {
      log.info("Recorded temperature {} with id {} on device {}", value, requestId, deviceId)
      lastTemperatureReading = Some(value)
      sender.tell(TemperatureRecorded(requestId), self)
    }
  }
}

