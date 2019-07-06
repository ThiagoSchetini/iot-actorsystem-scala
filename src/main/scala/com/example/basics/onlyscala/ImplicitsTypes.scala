package com.example.basics.onlyscala

final case class Json(value: String)
final case class Person(document: String, age: Int, profession: String)
final case class Customer(name: String, value: Double, genre: Char)
final case class Planet(planet: String, distance: Double)
final case class SomeOtherClass(something: String)


trait Jsonable[T] {
  def serialize(t: T): Json
}


object Jsonable {

  implicit object JsonablePerson extends Jsonable[Person] {
    override def serialize(t: Person): Json = {
      Json( s"""
                |{
                |"document": "${t.document}",
                |"age": "${t.age}",
                |"profession": "${t.profession}"
                |}
      """.stripMargin)
    }
  }

  implicit object JsonableCustomer extends Jsonable[Customer] {
    override def serialize(t: Customer): Json = {
      Json( s"""
               |{
               |"name": "${t.name}",
               |"value": "${t.value}",
               |"genre": "${t.genre}"
               |}
      """.stripMargin)
    }
  }

  implicit object JsonablePlanet extends Jsonable[Planet] {
    override def serialize(t: Planet): Json = {
      Json( s"""
               |{
               |"planet": "${t.planet}",
               |"distance": "${t.distance}"
               |}
      """.stripMargin)
    }
  }

  // easy way
  def convertToJson[T](t: T)(implicit converter: Jsonable[T]): Json = converter.serialize(t)

  // elegant way?
  //def convertToJson[T: Jsonable](t: T): Json = implicitly[Jsonable[T]].serialize(t)
}


object ImplicitsTypes extends App {

  val customerJson = Jsonable.convertToJson(Customer("Thiago", 913.43, 'M'))
  val planetJson = Jsonable.convertToJson(Planet("Pluto", 5432.44))
  val personJson = Jsonable.convertToJson(Person("34532143-X", 34, "Programmer"))

  println(customerJson.value)
  println(planetJson.value)
  println(personJson.value)

  /* GREAT! compilation fails: could not find implicit value for parameter Jsonable[SomeOtherClass] */
  //Jsonable.convertToJson(SomeOtherClass("anything"))
}
