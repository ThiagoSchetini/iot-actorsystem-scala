package com.example.iot

import akka.actor.ActorSystem
import akka.testkit.TestKit
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpec, WordSpecLike}

class DeviceSpec (_system: ActorSystem) extends TestKit(_system) with WordSpecLike with Matchers with BeforeAndAfterAll {

  //
  // creating an actor system and
  //
  def this() = this(ActorSystem("test-system"))

}


class Xx (val number: Double) {


  this(number:Double)
}

object Xx extends App {
  val some = new Xx(33d)
  println(some)
}

class ClassAuxiliaryConstructor(private var obrigatoryNumber:Double) {

  var anotherNumber:Int = 12
  var someString:String = "anything"
  var someChar:Char = 'a'

  def this(obrigatoryNumber:Double, anotherNumber: Int) {
    this(obrigatoryNumber)
    this.anotherNumber = anotherNumber
  }

  def this(obrigatoryNumber:Double, anotherNumber: Int, someString: String) {
    this(obrigatoryNumber, anotherNumber)
    this.someString = someString
  }

  def this(obrigatoryNumber:Double, anotherNumber: Int, someString: String, someChar: Char) {
    this(obrigatoryNumber, anotherNumber, someString)
    this.someChar = someChar
  }

  override def toString: String =
    s"""
       |obrigatory = $obrigatoryNumber
       |another = $anotherNumber
       |string = $someString
       |char = $someChar
  """.stripMargin
}
