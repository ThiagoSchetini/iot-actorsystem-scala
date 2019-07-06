package com.example.iotsystem

import java.util.concurrent.TimeUnit

import akka.actor.ActorSystem
import akka.testkit.{TestKit, TestProbe}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

import scala.concurrent.duration.FiniteDuration


/**
  * @param testSystem -> define the primary constructor of this class (and of course a val testSystem without getter)
  *
  * the unique constructor of TestKit needs an ActorSystem object -> TestKit​(ActorSystem system)
  * testSystem is passed to the constructor of TestKit -> ... extends TestKit(testSystem)
  */
class DeviceSpec (testSystem: ActorSystem) extends TestKit(testSystem)
  with WordSpecLike with Matchers with BeforeAndAfterAll {

  //
  // creating the default constructor
  // default invokes the defined primary constructor -> class DeviceSpec (testSystem: ActorSystem)...
  //
  def this() = this(ActorSystem.create("test-system"))

  override def afterAll(): Unit = shutdown(testSystem)

  "A Device Actor" should {

    "reply empty reading if no temperature is known" in {
      val probe = TestProbe()
      val deviceActor = testSystem.actorOf(Device.props("group", "device"))

      deviceActor.tell(Device.ReadTemperature(49L), probe.ref)
      val response = probe.expectMsgType[Device.RespondTemperature]
      response.requestId should ===(49L)
      response.value should ===(None)
    }

    "reply latest temperature reading" in {
      val probe = TestProbe()
      val deviceActor = testSystem.actorOf(Device.props("group", "device"))


      // recording and reading first temperature
      deviceActor.tell(Device.RecordTemperature(12L, 55.4), probe.ref)
      probe.expectMsgType[Device.TemperatureRecorded]

      deviceActor.tell(Device.ReadTemperature(13L), probe.ref)
      val response1 = probe.expectMsgType[Device.RespondTemperature]
      response1.requestId shouldBe 13L
      response1.value shouldBe Some(55.4)


      // recording and reading second temperature
      deviceActor.tell(Device.RecordTemperature(14L, 73.2), probe.ref)
      probe.expectMsgType[Device.TemperatureRecorded]

      deviceActor.tell(Device.ReadTemperature(15L), probe.ref)
      val response2 = probe.expectMsgType[Device.RespondTemperature]
      response2.requestId shouldBe 15L
      response2.value shouldBe Some(73.2)
    }

    "register a device" in {
      val probe = TestProbe()
      val deviceActor = testSystem.actorOf(Device.props("group", "device"))

      deviceActor.tell(DeviceManager.RequestTrackDevice(11L, "group", "device"), probe.ref)
      val response = probe.expectMsgType[DeviceManager.DeviceRegistered]
      response.requestId shouldBe 11L
      probe.lastSender shouldBe deviceActor
    }

    "ignore device registering if the groupId or deviceId are wrong" in {
      val probe = TestProbe()
      val deviceActor = testSystem.actorOf(Device.props("group", "device"))

      deviceActor.tell(DeviceManager.RequestTrackDevice(13L, "wrong-group", "device"), probe.ref)
      probe.expectNoMessage(FiniteDuration.apply(500, TimeUnit.MILLISECONDS))

      deviceActor.tell(DeviceManager.RequestTrackDevice(14L, "group", "wrong-device"), probe.ref)
      probe.expectNoMessage(FiniteDuration.apply(500, TimeUnit.MILLISECONDS))
   }

  }
}