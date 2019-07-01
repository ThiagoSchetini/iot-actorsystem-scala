package com.example.basics

import java.util.concurrent.{ExecutorService, Executors}
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}
import grizzled.slf4j.Logging


/**
  * case 1 -> dependency injection use case
  * note: the companion object receives it's own class implicit instance (Prefixer "injects" itself)

object Prefixer {
  implicit val p: Prefixer = Prefixer("***implicit***")
}

final case class Prefixer(prefix: String)

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
  * case 2 -> the same of case 1: the companion object receives it's own class implicit instance
  */
final case class Blood(color: String)
final case class User(name: String, blood: Blood)
final case class Credentials(id: String)

final case class Beast(credentials: Credentials) {
  def rape(user:User): String = user.name + " rapped by " + credentials.id
}

object BeastVault {
  /* compiler scan the implicit reference value here, on BeastVault own companion OBJECT */
  implicit val beastVault: BeastVault = BeastVault(Map.empty[String, Beast])
}

final case class BeastVault(var beasts: Map[String, Beast])

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


/**
  * case 3 -> simple dependency injection use case
  */
final case class ConnectionFactory(table: String) {

  /* the implicit declared here does not work! */
  // implicit val bananas: Map[String, String] = Map.empty[String, String]

  def getConnection(implicit params: Map[String, String]): String = "connectionMock"
}

object ConnectionFactory {

  /* the implicit declared here does not work too! It's not of type ConnectionFactory */
  // implicit val bananas: Map[String, String] = Map.empty[String, String]

  implicit val bananas: Map[String, String] = Map.empty[String, String]
}

object Connecting extends App {

  /* DANGER ? any value passed as implicit here of type Map[String, String] is accepted as "params" */
  implicit val bananas: Map[String, String] = Map.empty[String, String]

  val factory = ConnectionFactory("TableMock")
  val conn = factory.getConnection
  print(conn + " connected ...")
}

  */



class Positives(val n: Int)
class Negatives(val n: Int)

object Positives {
  implicit def one: Positives = new Positives(1)
  implicit def two: Negatives = new Negatives(-2)
  implicit val three: Int = 3
}

class SomeClass {
  def calcSum(implicit positives:Positives): Int = positives.n + 5
  def calcMultiply(implicit negatives:Negatives): Int= negatives.n * 5
}

trait M {

  // says that "M cannot be mixed into a concrete class that does not also extend Method"
  this: SomeClass =>

  implicit def eleven = new Positives(11)
  implicit def twelve = new Negatives(-22)

  def testy = calcSum
  def testx = calcMultiply

}


trait SM extends M {

  this: SomeClass =>

  implicit def x2 = new Positives(22)
  implicit def y2 = new Negatives(222)

  def testy2 = :/
  def testx2 = :\

}


object TestThis extends App {

  // implicit resolved from companion object
  val theClassInstance = new SomeClass()
  theClassInstance.:/


  // explicit applied so that value is used = 33
  new SomeClass().someMethod(new Positives(33))

  // will never use implicit inside M. implicit resolved from companion object of X = 1
  (new SomeClass with M).someMethod

  // local scope overrides companion object implicit = 30 and 30 again
  implicit def x = new Positives(30)

  new SomeClass().someMethod
  (new SomeClass with M).someMethod

  // again, explicit applied so that value is used = 5
  new SomeClass().someMethod(new Positives(5))


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




