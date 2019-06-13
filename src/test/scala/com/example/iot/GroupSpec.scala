package com.example.iot

import java.util.concurrent.TimeUnit

import akka.actor.{ActorSystem, PoisonPill}
import akka.testkit.{TestKit, TestProbe}
import com.example.iot.Device.{RecordTemperature, TemperatureRecorded}
import com.example.iot.DeviceGroup.{DeviceNotAvailable, DeviceTimedOut, ReplyAllTemperatures, ReplyDeviceList, RequestAllTemperatures, RequestDeviceList, Temperature, TemperatureNotAvailable}
import com.example.iot.DeviceManager.{DeviceRegistered, RequestTrackDevice}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

import scala.concurrent.duration.FiniteDuration

class GroupSpec(testSystem: ActorSystem) extends TestKit(testSystem)
  with WordSpecLike with Matchers with BeforeAndAfterAll {

  def this() = this(ActorSystem.create("test-system"))

  override def afterAll(): Unit = shutdown(testSystem)

  "A group actor" should {

    "register a device actor" in {
      val probe = TestProbe()
      val groupActor = testSystem.actorOf(DeviceGroup.props("groupA"))

      groupActor.tell(RequestTrackDevice(11L, "groupA", "deviceX"), probe.ref)
      probe.expectMsg(DeviceRegistered(requestId = 11L))
      val device1 = probe.lastSender

      groupActor.tell(RequestTrackDevice(12L, "groupA", "deviceY"), probe.ref)
      probe.expectMsg(DeviceRegistered(12L))
      val device2 = probe.lastSender

      device1 should !==(device2)

      // check devices are working
      device1.tell(RecordTemperature(14L, 39.8), probe.ref)
      probe.expectMsg(TemperatureRecorded(14L))

      device2.tell(RecordTemperature(17L, 43.1), probe.ref)
      probe.expectMsg(TemperatureRecorded(requestId = 17L))
    }

    "returns the same device and temperature for the same id" in {
      val probe = TestProbe()
      val groupActor = testSystem.actorOf(DeviceGroup.props("groupT"))

      groupActor.tell(RequestTrackDevice(13L, "groupT", "device0"), probe.ref)
      probe.expectMsg(DeviceRegistered(13L))
      val device0 = probe.lastSender
      device0.tell(Device.RecordTemperature(99L, 44.9), probe.ref)
      probe.expectMsg(Device.TemperatureRecorded(99L))

      // returns the same temperature to proof that is the same
      groupActor.tell(RequestTrackDevice(12L, "groupT", "device0"), probe.ref)
      probe.expectMsg(DeviceRegistered(12L))
      val device0Again = probe.lastSender
      device0Again.tell(Device.ReadTemperature(98L), probe.ref)
      val response = probe.expectMsgType[Device.RespondTemperature]
      response.requestId shouldBe 98L
      response.value.get shouldBe 44.9

      // check they are the same
      device0 shouldEqual device0Again
    }

    "not register a device with wrong groupId" in {
      val probe = TestProbe()
      val groupActor = testSystem.actorOf(DeviceGroup.props("groupB"))

      groupActor.tell(RequestTrackDevice(19L, "groupWrong", "deviceX"), probe.ref)
      probe.expectNoMessage(FiniteDuration.apply(500, TimeUnit.MILLISECONDS))
    }

    "list active devices" in {
      val probe = TestProbe()
      val groupActor = testSystem.actorOf(DeviceGroup.props("groupX"))

      groupActor.tell(RequestTrackDevice(1L, "groupX", "device1"), probe.ref)
      probe.expectMsgType[DeviceRegistered]

      groupActor.tell(RequestTrackDevice(2L, "groupX", "device2"), probe.ref)
      probe.expectMsgType[DeviceRegistered]

      groupActor.tell(RequestDeviceList(33L), probe.ref)
      probe.expectMsg(ReplyDeviceList(33L, Set("device1", "device2")))
    }

    "list active device after one shuts down" in {
      val probe = TestProbe()
      val groupActor = testSystem.actorOf(DeviceGroup.props("groupS"))

      groupActor.tell(RequestTrackDevice(71L, "groupS", "device1"), probe.ref)
      probe.expectMsgType[DeviceRegistered]

      groupActor.tell(RequestTrackDevice(72L, "groupS", "device2"), probe.ref)
      probe.expectMsgType[DeviceRegistered]
      val toShutDown = probe.lastSender

      probe.watch(toShutDown)
      toShutDown.tell(PoisonPill.getInstance, probe.ref)
      probe.expectTerminated(toShutDown)

      probe.awaitAssert {
        groupActor.tell(RequestDeviceList(91L), probe.ref)
        probe.expectMsg(ReplyDeviceList(91L, Set("device1")))
      }
    }

    "collect temperatures from all devices" in {

      // test configs
      val probe = TestProbe()
      val groupActor = system.actorOf(DeviceGroup.props("group"))


      // creating the actors
      groupActor.tell(RequestTrackDevice(1L, "group", "device1"), probe.ref)
      probe.expectMsgType[DeviceRegistered]
      probe.lastSender.tell(RecordTemperature(0L, 36.5), probe.ref)
      probe.expectMsgType[TemperatureRecorded]

      groupActor.tell(RequestTrackDevice(2L, "group", "device2"), probe.ref)
      probe.expectMsgType[DeviceRegistered]
      probe.lastSender.tell(RecordTemperature(0L, 89.7), probe.ref)
      probe.expectMsgType[TemperatureRecorded]

      groupActor.tell(RequestTrackDevice(3L, "group", "device3"), probe.ref)
      probe.expectMsgType[DeviceRegistered]
      // no temperature for device3


      // reply
      val timeout = FiniteDuration.apply(2, TimeUnit.SECONDS)
      groupActor.tell(RequestAllTemperatures(99L, timeout), probe.ref)

      probe.expectMsg(ReplyAllTemperatures(99L, Map(
        "device1" -> Temperature(36.5),
        "device2" -> Temperature(89.7),
        "device3" -> TemperatureNotAvailable
      )))

    }

  }
}
