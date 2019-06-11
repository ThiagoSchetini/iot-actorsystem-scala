package com.example.iot

import java.util.concurrent.TimeUnit

import akka.actor.{ActorSystem, PoisonPill}
import akka.testkit.{TestKit, TestProbe}
import com.example.iot.Device.{ReadTemperature, RespondTemperature}
import com.example.iot.DeviceGroup.{DeviceNotAvailable, DeviceTimedOut, ReplyAllTemperatures, Temperature, TemperatureNotAvailable}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

import scala.concurrent.duration.FiniteDuration

class QuerySpec(testSystem: ActorSystem) extends TestKit(testSystem)
  with WordSpecLike with Matchers with BeforeAndAfterAll{

  def this() = this(ActorSystem("test-system"))

  override def afterAll(): Unit = shutdown(testSystem)

  "a device group query" should {

    "return temperatures for working devices" in {
      val requester = TestProbe()
      val device1 = TestProbe()
      val device2 = TestProbe()
      val actorToDeviceId = Map(device1.ref -> "device1", device2.ref -> "device2")
      val timeout = FiniteDuration.apply(2, TimeUnit.SECONDS)

      val queryActor = system.actorOf(DeviceGroupQuery.props(actorToDeviceId, 9L, requester.ref, timeout))

      device1.expectMsg(Device.ReadTemperature(0L))
      device2.expectMsg(Device.ReadTemperature(0L))

      queryActor.tell(RespondTemperature(0L, Some(29.2)), device1.ref)
      queryActor.tell(RespondTemperature(0L, Some(39.9)), device2.ref)

      requester.expectMsg(ReplyAllTemperatures(9L, Map("device1" -> Temperature(29.2), "device2" -> Temperature(39.9))))
    }

    "return TemperatureNotAvailable for devices with no readings" in {
      val requester = TestProbe()
      val device1 = TestProbe()
      val device2 = TestProbe()
      val actorToDeviceId = Map(device1.ref -> "device1", device2.ref -> "device2")
      val timeout = FiniteDuration.apply(2, TimeUnit.SECONDS)

      val queryActor = system.actorOf(DeviceGroupQuery.props(actorToDeviceId, 11L, requester.ref, timeout))

      device1.expectMsg(Device.ReadTemperature(0L))
      device2.expectMsg(Device.ReadTemperature(0L))

      queryActor.tell(RespondTemperature(0L, Some(31.33)), device1.ref)
      queryActor.tell(RespondTemperature(0L, None), device2.ref)

      requester.expectMsg(
        ReplyAllTemperatures(11L, Map("device1" -> Temperature(31.33), "device2" -> TemperatureNotAvailable)))
    }

    "return DeviceNotAvailable if device stops before answering" in {
      val requester = TestProbe()
      val device1 = TestProbe()
      val device2 = TestProbe()
      val actorToDeviceId = Map(device1.ref -> "device1", device2.ref -> "device2")
      val timeout = FiniteDuration.apply(2, TimeUnit.SECONDS)

      val queryActor = system.actorOf(DeviceGroupQuery.props(actorToDeviceId, 13L, requester.ref, timeout))

      device1.expectMsg(Device.ReadTemperature(0L))
      device2.expectMsg(Device.ReadTemperature(0L))

      queryActor.tell(RespondTemperature(0L, Some(33.4)), device1.ref)
      device2.ref ! PoisonPill

      requester.expectMsg(ReplyAllTemperatures(13L, Map("device1" -> Temperature(33.4), "device2" -> DeviceNotAvailable)))
    }

    "return temperatures even if device stops after response" in {
      val requester = TestProbe()
      val device1 = TestProbe()
      val device2 = TestProbe()
      val actorToDeviceId = Map(device1.ref -> "device1", device2.ref -> "device2")
      val timeout = FiniteDuration.apply(2, TimeUnit.SECONDS)

      val queryActor = system.actorOf(DeviceGroupQuery.props(actorToDeviceId, 15L, requester.ref, timeout))

      device1.expectMsg(Device.ReadTemperature(0L))
      device2.expectMsg(Device.ReadTemperature(0L))

      queryActor.tell(RespondTemperature(0L, Some(22.5)), device1.ref)
      queryActor.tell(RespondTemperature(0L, Some(11.3)), device2.ref)
      device2.ref ! PoisonPill

      requester.expectMsg(ReplyAllTemperatures(15L, Map("device1" -> Temperature(22.5), "device2" -> Temperature(11.3))))
    }

    "return DeviceTimedOut if device does not answer in time" in {
      val requester = TestProbe()
      val device1 = TestProbe()
      val device2 = TestProbe()
      val actorToDeviceId = Map(device1.ref -> "device1", device2.ref -> "device2")
      val x = FiniteDuration.apply(7, TimeUnit.SECONDS)

      val queryActor = system.actorOf(DeviceGroupQuery.props(actorToDeviceId, 19L, requester.ref, x))

      device1.expectMsg(ReadTemperature(0L))
      device2.expectMsg(ReadTemperature(0L))

      queryActor.tell(Temperature(55.9), device1.ref)

      requester.expectMsg(ReplyAllTemperatures(19L, Map("device1" -> Temperature(55.9), "device2" -> DeviceTimedOut)))
    }

  }
}
