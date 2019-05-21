package com.example.iot

import akka.actor.{Actor, ActorLogging, Props}
import com.example.iot.Device.{ReadTemperature, RecordTemperature, RespondTemperature, TemperatureRecorded}
import com.example.iot.DeviceManager.DeviceRegistered

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
    case DeviceManager.RequestTrackDevice(id, group, device) => {
      if (group == groupId && device == deviceId) {
        sender.tell(DeviceRegistered, self)
      } else {
        log.warning(
          s"Ignoring track request $id for group {} and device {}. This actor responsible for group {} device {}",
          group, device, this.groupId, this.deviceId
        )
      }
    }

    case ReadTemperature(id) => sender.tell(RespondTemperature(id, lastTemperatureReading), self)

    case RecordTemperature(id, value) => {
      log.info("Recorded temperature {} with id {} on device {}", value, id, deviceId)
      lastTemperatureReading = Some(value)
      sender.tell(TemperatureRecorded(id), self)
    }
  }
}

