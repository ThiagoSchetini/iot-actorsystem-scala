package com.example.basics

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}


/**
  * case 1 -> dependency injection use case
  * note: the companion object receives it's own class implicit instance (Prefixer "injects" itself)
  */
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