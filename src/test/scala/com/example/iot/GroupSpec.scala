package com.example.iot

import java.util.concurrent.TimeUnit

import akka.actor.ActorSystem
import akka.testkit.{TestKit, TestProbe}
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
      device1.tell(Device.RecordTemperature(14L, 39.8), probe.ref)
      probe.expectMsg(Device.TemperatureRecorded(14L))

      device2.tell(Device.RecordTemperature(17L, 43.1), probe.ref)
      probe.expectMsg(Device.TemperatureRecorded(requestId = 17L))
    }

    "existing device responds as registered" in {
      val probe = TestProbe()
      val groupActor = testSystem.actorOf(DeviceGroup.props("groupT"))

      groupActor.tell(RequestTrackDevice(13L, "groupT", "device0"), probe.ref)
      probe.expectMsg(DeviceRegistered(13L))
      val device0 = probe.lastSender
      device0.tell(Device.RecordTemperature(99L, 44.9), probe.ref)
      probe.expectMsg(Device.TemperatureRecorded(99L))

      groupActor.tell(RequestTrackDevice(12L, "groupT", "device0"), probe.ref)
      probe.expectMsg(DeviceRegistered(12L))
      val device0Again = probe.lastSender
      device0Again.tell(Device.ReadTemperature(98L), probe.ref)
      val response = probe.expectMsgType[Device.RespondTemperature]
      response.requestId shouldBe 98L
      response.value.get shouldBe 44.9
    }

    "not register a device with wrong groupId" in {
      val probe = TestProbe()
      val groupActor = testSystem.actorOf(DeviceGroup.props("groupB"))

      groupActor.tell(RequestTrackDevice(19L, "groupWrong", "deviceX"), probe.ref)
      probe.expectNoMessage(FiniteDuration.apply(500, TimeUnit.MILLISECONDS))
    }

   }
}
