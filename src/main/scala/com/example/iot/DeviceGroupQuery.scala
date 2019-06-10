package com.example.iot

import akka.actor.{Actor, ActorLogging, ActorRef, Props, Terminated}
import com.example.iot.Device.RespondTemperature
import com.example.iot.DeviceGroup.{DeviceNotAvailable, DeviceTimedOut, ReplyAllTemperatures, Temperature, TemperatureNotAvailable, TemperatureReading}
import com.example.iot.DeviceGroupQuery.CollectionTimedOut

import scala.concurrent.duration.FiniteDuration

object DeviceGroupQuery {
  case object CollectionTimedOut

  def props(
             actorToDeviceId: Map[ActorRef, String],
             requestId: Long,
             requester: ActorRef,
             timeout: FiniteDuration): Props = {
    Props(new DeviceGroupQuery(actorToDeviceId, requestId, requester, timeout))
  }

}

class DeviceGroupQuery(
                        actorToDeviceId: Map[ActorRef, String],
                        requestId: Long,
                        requester: ActorRef,
                        timeout: FiniteDuration)
  extends Actor with ActorLogging {

  val queryTimeoutTimer = context.system.scheduler.scheduleOnce(timeout, self, CollectionTimedOut)

  override def preStart(): Unit = {
    actorToDeviceId.keysIterator.foreach {
      deviceActor =>
        context.watch(deviceActor)
        deviceActor.tell(Device.ReadTemperature(0L), self)
    }
  }

  override def postStop(): Unit = {
    queryTimeoutTimer.cancel()
  }

  override def receive: Receive = waitingForReplies(Map.empty, actorToDeviceId.keySet)

  def waitingForReplies(replies: Map[String, TemperatureReading], waiting: Set[ActorRef]): Receive = {

    case RespondTemperature(0L, valueOption) =>
      val reading = valueOption match {
        case Some(value) => Temperature(value)
        case None => TemperatureNotAvailable
      }
      receivedResponse(sender(), reading, replies, waiting)

    case Terminated(deviceActor) => receivedResponse(deviceActor, DeviceNotAvailable, replies, waiting)

    case CollectionTimedOut =>
      val timedOutReplies = waiting.map {deviceActor =>
        val deviceId = actorToDeviceId(deviceActor)
        deviceId -> DeviceTimedOut
      }
      this.requester.tell(ReplyAllTemperatures(this.requestId, replies ++ timedOutReplies), self)
      context.stop(self)
  }

  def receivedResponse(
                        sender: ActorRef,
                        reading: TemperatureReading,
                        replies: Map[String, TemperatureReading],
                        waiting: Set[ActorRef]): Unit = {

    context.unwatch(sender)
    val waitingSoFar = waiting.-(sender)
    val replySoFar = replies.+(actorToDeviceId(sender) -> reading)

    if(waitingSoFar.isEmpty) {
      this.requester.tell(ReplyAllTemperatures(this.requestId, replySoFar), self)
      context.stop(self)
    } else {
      context.become(waitingForReplies(replySoFar, waitingSoFar))
    }
  }

}
