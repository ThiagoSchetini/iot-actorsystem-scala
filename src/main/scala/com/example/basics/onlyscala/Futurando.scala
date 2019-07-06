package com.example.basics.onlyscala

import grizzled.slf4j.Logging

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Future, blocking}

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


/**
  * with for comprehension
  */
object FutureWithFor extends App with Logging {

  val amount = 50

  val usdQuote = Future {
    Thread.sleep(2000)
    info("finalized usdQuote")
    35
  }

  val chfQuote = Future {
    Thread.sleep(2000)
    info("finalized chfQuote")
    33
  }

  def buy(amount: Int, x: Int): Future[Int] = Future { amount *  x}

  def isProfitable(number: Int, number2: Int): Boolean = {if( number > 32) true else false}

  val purchase = for {
    usd <- usdQuote     // executes at same time (async)
    chf <- chfQuote     // executes at same time (async)
    if isProfitable(usd, chf)
  } yield buy(amount, chf)

  purchase foreach { total =>
    info("Purchased " + total.value.get.get + " CHF") // Future.Some.Success.Int
  }

  Thread.sleep(4000)
}


/**
  * The for-comprehension above is translated into more recursive thing
  */
object FutureRecursive extends App with Logging {

  val amount = 50

  val usdQuote = Future {
    Thread.sleep(2000)
    info("finalized usdQuote")
    35
  }

  val chfQuote = Future {
    Thread.sleep(2000)
    info("finalized chfQuote")
    33
  }

  def buy(amount: Int, x: Int): Future[Int] = Future { amount *  x}

  def isProfitable(number: Int, number2: Int): Boolean = {if( number > 32) true else false}


  // usdQuote and chfQuote executes async at same time
  val purchase = usdQuote flatMap {                   // flatMap maps its own value into some other future
    usd => chfQuote                                   // map the value of the chfQuote into a third future
      .filter(chf => isProfitable(usd, chf))          // third future created here
      .map(chf => buy(amount, chf))                   // fourth future created here
  }


  purchase foreach { total =>
    info("Purchased " + total.value.get.get + " CHF") // Future.Some.Success.Int
  }

  Thread.sleep(4000)
}