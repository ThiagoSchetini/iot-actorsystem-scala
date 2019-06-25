package com.example.basics


import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import java.time.Instant
import grizzled.slf4j.Logging

/**
  * tried to simulate the volatile variable on main memory, but failed
  */
object Volatile extends App with Logging {

  @volatile var x = 0
  val futureStr: Future[String] = Future {"placebo"}

  // concurrent threads for read
  for(_ <- 1 to 200) {

     // no @volatile x -> Do not have guarantee that read will be updated
     // with @volatile -> has guarantee of updated reading because uses only main memory (low performance)
    futureStr foreach { _ =>
      Thread.sleep(1)
      info("[READ THREAD] x = " + x + " on " + Instant.now().getNano.toString)
    }
  }

  // no concurrent threads for write
  for(_ <- 1 to 50) {

    // no @volatile x -> copy the value of main memory to cache (L1?), calculates, can't guarantee main memory updates
    // with @volatile -> no copy at all. uses only on the main memory (low performance)
    Thread.sleep(1)
    x += 1
    info("[WRITE] x = " + x + " on " + Instant.now().getNano.toString)
  }

  Thread.sleep(2000)
}
