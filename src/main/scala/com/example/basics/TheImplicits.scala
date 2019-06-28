package com.example.basics

import java.util.concurrent.Executors

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}


/**
  * case 1 -> sofisticated dependency injection use case
  * note: the companion object receives it's own class implicit instance (Prefixer "injects" itself)

final case class Prefixer(prefix: String)

object Prefixer {
  implicit val p: Prefixer = Prefixer("***implicit***")
}

class Executor {
  /* compiler scan the implicit Prefixer value here, on it's companion object */
  def prefixThis(s: String)(implicit p: Prefixer):String = p.prefix + s
}

object Test extends App {
  val executor = new Executor
  println(executor.prefixThis("banana")) /* calls with implicit */
  println(executor.prefixThis("banana")(Prefixer("---explicit---")))
}


/**
  * case 2 -> dangerous dependency injection use case
  */
final case class ConnectionFactory(table: String) {
  def getConnection(implicit params: Map[String, String]): String = "connectionMock"
}

object Connecting extends App {

  /*
   * compiler wouldn't scan the implicit Map value on a ConnectionFactory companion object
   * DANGER: any value passed here with type Map[String, String] is accepted as "params"
   */
  implicit val banana: Map[String, String] = Map.empty[String, String]

  val factory = ConnectionFactory("TableMock")
  val conn = factory.getConnection
  print(conn + "connected ...")
}


/**
  * case 3 ->
  * note: the companion object receives it's own class implicit instance
  */
final case class Blood(color: String)
final case class User(name: String, blood: Blood)
final case class Credentials(id: String)

final case class Beast(credentials: Credentials) {
  def rape(user:User): String = user.name + " rapped by " + credentials.id
}

final case class BeastVault(var beasts: Map[String, Beast])

object BeastVault {
  /* compiler scan the implicit reference value here, on it's companion OBJECT */
  implicit val beastVault: BeastVault = BeastVault(Map.empty[String, Beast])
}

final case class Hell(implicit beastVault: BeastVault) {

  def createBeastFor(id: Credentials): Beast = {
    val newOne = Beast(id)
    beastVault.beasts += newOne.credentials.id -> newOne
    newOne
  }
}

object RapeTheUser extends App {

  /* BeastVault doesn't need to be imported because it was implicitly created */
  final val hell: Hell = Hell()
  val user = User("Omen", Blood("red"))
  val beast = hell.createBeastFor(Credentials("BeastX86"))

  /* need to import scala.concurrent.ExecutionContext.Implicits.global to have the ExecutionContext */
  val f: Future[Option[Blood]] = Future {
    val rappedMsg = beast.rape(user)
    println(rappedMsg)
    Option(user.blood)
  }

  f onComplete {
    case Success(opt) => println("rape1: the user blood color was " + opt.get.color)
    case Failure(t) => println("An error has occurred: " + t.getMessage)
  }

  val f2: Future[Option[Blood]] = Future {
    val rappedMsg = beast.rape(user)
    println(rappedMsg)
    Option(user.blood)
  }

  f2.onComplete {
    case Success(opt) => println("rape2: the user blood color was " + opt.get.color)
    case Failure(t) => println("An error has ocurred: " + t.getMessage)
  }

  Thread.sleep(1000)
}
  */


/*
class Something(val someVar: Int)
class Another(val anotherVar: Int)

object Something {
  implicit def qualquerPorra: Something = new Something(999)
  implicit def outraPorra: Another = new Another(21)
  implicit val bosta: Int = 29

}

class SomeClass {
  def someMethod(implicit someMoreImplicitVar:Something) = println(someMoreImplicitVar.someVar)
  def anotherMethod(implicit anotherMoreImplicitVar:Another)= println(anotherMoreImplicitVar.anotherVar)
}

trait M {

  // says that "M cannot be mixed into a concrete class that does not also extend Method"
  this: SomeClass =>

  implicit def x1 = new Something(11)
  implicit def y1 = new Another(111)

  def testy = anotherMethod
  def testx = someMethod

}


trait SM extends M {

  this: SomeClass =>

  implicit def x2 = new Something(22)
  implicit def y2 = new Another(222)

  def testy2 = anotherMethod
  def testx2 = someMethod

}


object TestThis extends App {

  // implicit resolved from companion object
  val theClassInstance = new SomeClass()
  theClassInstance.someMethod


  // explicit applied so that value is used = 33
  new SomeClass().someMethod(new Something(33))

  // will never use implicit inside M. implicit resolved from companion object of X = 1
  (new SomeClass with M).someMethod

  // local scope overrides companion object implicit = 30 and 30 again
  implicit def x = new Something(30)

  new SomeClass().someMethod
  (new SomeClass with M).someMethod

  // again, explicit applied so that value is used = 5
  new SomeClass().someMethod(new Something(5))


  /**
    * now, using the scope of the traits
    */

  // testx is defined within M so the implicits within M overrides the companion object implicit = 11
  (new SomeClass with M).testx

  // testx is within M (not SM) so the implicit within M is used = 11
  (new SomeClass with SM).testx

  // testx2 is within SM so the implicit within SM overrides the implicit in M and the companion object = 22
  (new SomeClass with SM).testx2
}
*/

