package com.example.iot

import akka.actor.ActorSystem
import akka.testkit.TestKit
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}


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

}