package com.example.basics.onlyscala

object RecursiveAPI extends App {

  val list = List(1,2,3,4)
  val strings = List("abc", "bcd", "edex")
  val map = Map("xa" -> 5, "yb" -> 7, "z3" -> 99)


  // filter = only filter by Boolean
  println(list.filter(_ > 2))                     // n => n > 4


  // map = operation on each element
  println(list.map(_ + 1))                        // n => n + 1
  println(strings.map(_.concat("::123")))   // str => str.concat("::123")


  // scan = operation two by two with one on start concatenating individual results (outs all them)
  println(list.scan(10)(_ + _))                   // (a, b) => a + b
  println(list.scan(0)(_ + _))


  // flatMap = map + flatten
  println(strings.flatMap(_.concat("::123")))
  println(map.flatMap(a => if (a._2 > 5) Some(a._1, a._2) else None))
  println(map.flatMap{ case (k, v) => if (v > 5) Some(k, v) else None})


  // reduce = operation two by two concatenating only the final result outs
  println(list.reduceLeft((a, b) => (a + b) * 2 ))
  println(list.reduce((a, b) => (a + b) * 2 ))


  // fold = reduce, with one element on start (only final result outs too)
  println(list.foldLeft(10)(_ + _))               // (a, b) => a + b
  println(list.fold(10)(_ + _))
}
