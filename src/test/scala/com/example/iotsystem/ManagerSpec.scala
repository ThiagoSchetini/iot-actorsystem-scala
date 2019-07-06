package com.example.iotsystem

import akka.actor.{ActorSystem, PoisonPill}
import akka.testkit.{TestKit, TestProbe}
import com.example.iotsystem.DeviceManager.{DeviceRegistered, ReplyGroupList, ReplyGroupMap, RequestGroupList, RequestGroupMap, RequestTrackDevice}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

class ManagerSpec(testSystem: ActorSystem) extends TestKit(testSystem)
  with WordSpecLike with Matchers with BeforeAndAfterAll {

  def this() = this(ActorSystem.create("test-system"))
  override def afterAll(): Unit = shutdown(testSystem)

  "A device group manager actor" should {

    "list active groups" in {
      val probe = TestProbe()
      val managerActor = testSystem.actorOf(DeviceManager.props())

      managerActor.tell(RequestTrackDevice(81L, "groupY", "deviceA"), probe.ref)
      probe.expectMsgType[DeviceRegistered]

      managerActor.tell(RequestTrackDevice(99L, "groupX", "deviceA"), probe.ref)
      probe.expectMsgType[DeviceRegistered]

      managerActor.tell(RequestGroupList(31L), probe.ref)
      probe.expectMsg(ReplyGroupList(31L, Set("groupY", "groupX")))
    }

    "map active group after one shutdowns" in {
      val probe = TestProbe()
      val managerActor = testSystem.actorOf(DeviceManager.props())

      managerActor.tell(RequestTrackDevice(12L, "group1", "device1"), probe.ref)
      probe.expectMsgType[DeviceRegistered]

      managerActor.tell(RequestTrackDevice(13L, "group2", "device1"), probe.ref)
      probe.expectMsgType[DeviceRegistered]

      managerActor.tell(RequestGroupMap(14L), probe.ref)
      val toShutDownGroup = probe.expectMsgType[ReplyGroupMap].groupsMap("group1")
      probe.watch(toShutDownGroup)
      toShutDownGroup.tell(PoisonPill.getInstance, probe.ref)

      awaitAssert {
        managerActor.tell(RequestGroupList(15L), probe.ref)
        probe.expectMsg(ReplyGroupList(15L, Set("group2")))
      }
    }

    "not register a duplicated group actor" in {
      val probe = TestProbe()
      val managerActor = testSystem.actorOf(DeviceManager.props())

      managerActor.tell(RequestTrackDevice(28L, "groupOne", "deviceOne"), probe.ref)
      probe.expectMsgType[DeviceRegistered]

      managerActor.tell(RequestTrackDevice(29L, "groupOne", "deviceTwo"), probe.ref)
      probe.expectMsgType[DeviceRegistered]

      managerActor.tell(RequestGroupList(38L), probe.ref)
      probe.expectMsg(ReplyGroupList(38L, Set("groupOne")))
    }

  }
}
