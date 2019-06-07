package com.example.iot

import akka.actor.{ActorSystem, Props}
import akka.testkit.{TestKit, TestProbe}
import com.example.iot.DeviceManager.{DeviceRegistered, ReplyGroupList, RequestGroupList, RequestTrackDevice}
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

    "list active group after one shutdowns" in {

    }

    "not register a duplicated group actor" in {

    }

  }
}
