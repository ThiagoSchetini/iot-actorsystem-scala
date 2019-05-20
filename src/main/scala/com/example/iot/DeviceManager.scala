package com.example.iot

object DeviceManager {

  case class RequestTrackDevice(requestId: Long, groupId: String, deviceId: String)
  case object DeviceRegistered

}
