package com.example.basics.onlyscala

final case class Gson(value: String)
final case class Fruit(name: String, price: Double)


trait Gsonable[T] {
  def serialize(t: T): Gson
}


object Gsonable {

  implicit object JsonableFruit extends Gsonable[Fruit] {
    override def serialize(t: Fruit): Gson = {
      Gson( s"""
               |{
               |"name": "${t.name}",
               |"price": "${t.price}"
               |}
      """.stripMargin)
    }
  }

  // easy way
  //def convertToGson[T](t: T)(implicit converter: Gsonable[T]): Gson = converter.serialize(t)
  //def convertToGsonAndPrint[T](t: T)(implicit converter: Gsonable[T]): Unit = println(convertToGson(t).value)
  //def convertMultipleItemsToGson[T](t: Array[T])(implicit converter: Gsonable[T]): Array[Gson] = t.map(convertToGson(_))

  // elegant way?
  def convertToGson[T: Gsonable](t: T): Gson = implicitly[Gsonable[T]].serialize(t)
  def convertToGsonAndPrint[T: Gsonable](t: T): Unit = println(convertToGson(t).value)
  def convertMultipleItemsToGson[T: Gsonable](t: Array[T]): Array[Gson] = t.map(convertToGson(_))
}


object ImplicitsOverloading extends App {

  Gsonable.convertToGsonAndPrint(Fruit("banana", 1.23))

  val fruits = Gsonable.convertMultipleItemsToGson(Array(Fruit("apple", 3.99), Fruit("papaya", 4.99), Fruit("lemon", 1.33)))
  fruits.foreach(f => println(f.value))
}

