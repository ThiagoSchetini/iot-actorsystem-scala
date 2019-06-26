package com.example.basics


import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Future, blocking}
import grizzled.slf4j.Logging

/**
  * so, run and see what is the maximum number of Threads blocked that your PC is executing?
  */
object Futurando extends App with Logging {

  val processors = Runtime.getRuntime.availableProcessors()
  info("number of available processors: " + processors)

  for( i <- 1 to 32 ) {
    Future {
      blocking {
        info("[BLOCK] " + i)
        Thread.sleep(999999)
      }
    }
  }

  for( i <- 1 to 300 ) {
    Future {
      //info("[NO BLOCK] " + i)
      Thread.sleep(1)
    }.onComplete(_ => info("[NO BLOCK] " + i))
  }

  // my maximum number here on an old i5 is 228
  for( i <- 1 to 3000 ) {
    Future {
      blocking {
        info("[BLOCK] " + i)
        Thread.sleep(999999)
      }
    }
  }

  Thread.sleep(99999999)
}
