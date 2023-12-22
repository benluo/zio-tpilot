package com.rockthejvm

import zio.*

import scala.io.StdIn

object ZIORecap extends ZIOAppDefault:

  // basics
  val meaningOfLife: ZIO[Any, Nothing, Int] = ZIO.succeed(42)
  // failure
  val aFailure: ZIO[Any, String, Nothing] = ZIO.fail("something went wrong")
  // suspension
  val aSuspension: ZIO[Any, Throwable, Int] = ZIO.suspend(meaningOfLife)

  // map/flatMap
  val improvedMOL = meaningOfLife.map(_ * 2)
  val printingMOL: ZIO[Any, Nothing, Unit] = for
    mol <- meaningOfLife
    p <- ZIO.succeed(println(mol))
  yield p

  val smallProgram = for
    _ <- Console.printLine("what's your name?")
    name <- ZIO.succeed(StdIn.readLine())
    _ <- Console.printLine(s"Welcome to ZIO, $name")
  yield ()

  // error handling
  val anAttempt: Task[Int] = ZIO.attempt:
    println("trying something")
    val str: String = null
    str.length

  // catch errors effectfully
  val catchError: UIO[Int|String] = anAttempt.catchAll(_ => ZIO.succeed(s"Returning some different value"))
  val catchSelective: Task[Int|String] = anAttempt.catchSome:
    case e: RuntimeException => ZIO.succeed(s"ignoring runtime exception: $e")
    case _ => ZIO.succeed("ignoring everything else")

  // fibers
  val delayedValue: UIO[Int] = ZIO.sleep(1.second) *> Random.nextIntBetween(0, 100)

  val aPair: UIO[(Int, Int)] = for
    vA <- delayedValue
    vB <- delayedValue
  yield (vA, vB) // this takes 2 seconds

  val aPairPar: UIO[(Int, Int)] = for
    fibA <- delayedValue.fork
    fibB <- delayedValue.fork
    vA <- fibA.join
    vB <- fibB.join
  yield (vA, vB) // this takes 1 second (if you have multiple threads)

  val interruptedFiber: UIO[Unit] = for
    fib <- delayedValue.onInterrupt(ZIO.succeed(println("I'm interrupted"))).fork
    _ <- ZIO.sleep(500.millis) *> ZIO.succeed(println("cancelling fiber")) *> fib.interrupt
    _ <- fib.join
  yield ()

  val ignoredInterruption: UIO[Unit] = for
    fib <- ZIO
      .uninterruptible:
        delayedValue.map(println).onInterrupt(ZIO.succeed(println("I'm interrupted")))
      .fork
    _ <- ZIO.sleep(500.millis) *> ZIO.succeed(println("cancelling fiber")) *> fib.interrupt
    _ <- fib.join
  yield ()

  val aPairPar2: UIO[(Int, Int)] = delayedValue zipPar delayedValue
  val random10: UIO[Seq[Int]] = ZIO.collectAllPar((1 to 10).map(_ => delayedValue)) // "traverse"

  // dependencies

  case class User(name: String, email: String)

  class UserSubscription(emailService: EmailService, userDatabase: UserDatabase):
    def subscribeUser(user: User): Task[Unit] = for
      _ <- emailService.email(user)
      _ <- userDatabase.insert(user)
      _ <- ZIO.succeed(s"subscribed $user")
    yield ()
  object UserSubscription:
    val live: ZLayer[EmailService with UserDatabase, Nothing, UserSubscription] =
      ZLayer.fromFunction((emailS, userD) => UserSubscription(emailS, userD))

  class EmailService:
    def email(user: User): Task[Unit] = ZIO.succeed(s"Emailed $user")
  object EmailService:
    val live: ZLayer[Any, Nothing, EmailService] =
      ZLayer.succeed(EmailService())

  class UserDatabase(connPool: ConnectionPool):
    def insert(user: User): Task[Unit] = ZIO.succeed(s"inserted user")
  object UserDatabase:
    val live: ZLayer[ConnectionPool, Nothing, UserDatabase] =
      ZLayer.fromFunction(cp => UserDatabase(cp))

  class ConnectionPool(nConns: Int):
    def get: Task[Connection] = ZIO.succeed(Connection())
  object ConnectionPool:
    def live(nConns: Int): ZLayer[Any, Nothing, ConnectionPool] =
      ZLayer.succeed(ConnectionPool(nConns))

  case class Connection()

  def subscribe(user: User): ZIO[UserSubscription, Throwable, Unit] = for
    sub <- ZIO.service[UserSubscription]
    _ <- sub.subscribeUser(user)
  yield ()

  val program: ZIO[UserSubscription, Throwable, Unit] = for
    _ <- subscribe(User("Daniel", "daniel@rockthejvm.com"))
    _ <- subscribe(User("Bon Jovi", "jon@rockthejvm.com"))
  yield ()

  override def run: Task[Unit] = program.provide(
    ConnectionPool.live(9),
    UserDatabase.live,
    EmailService.live,
    UserSubscription.live
  )

