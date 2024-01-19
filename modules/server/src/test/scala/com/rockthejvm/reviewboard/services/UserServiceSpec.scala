package com.rockthejvm.reviewboard.services

import com.rockthejvm.reviewboard.domain.data.{RecoveryToken, User, UserId, UserToken}
import com.rockthejvm.reviewboard.repositories.{RecoveryTokensRepository, UserRepository}
import zio.*
import zio.test.*

object UserServiceSpec extends ZIOSpecDefault:
  private val daniel = User(
    1L,
    "daniel@rockthejvm.com",
    "1000:6138DE5D5322694F287FBF5E28FD85D9B6C5642A7F693BAD:3E587FE6FE26F25AF88F69E372B0D79720A3645669F677A5"
  )

  private def stubbedUserRepo = ZLayer.succeed:
      new UserRepository:
        private val db: collection.mutable.Map[Long, User] =
          collection.mutable.Map(daniel.id -> daniel)

        override def getByEmail(email: String): Task[Option[User]] =
          ZIO.succeed(db.values.find(_.email == email))

        override def create(user: User): Task[User] =
          ZIO.succeed:
              db += (user.id -> user)
              user

        override def update(id: Long, op: User => User): Task[User] =
          ZIO.attempt:
              val newUser = op(db(id))
              db += (newUser.id -> newUser)
              newUser

        override def delete(id: Long): Task[User] =
          ZIO.attempt:
              val user = db(id)
              db -= id
              user

        override def getById(id: Long): Task[Option[User]] =
          ZIO.succeed(db.get(id))

        override def getAll: Task[List[User]] =
          ZIO.succeed(db.values.toList)
  end stubbedUserRepo

  private def stubbedJwtService = ZLayer.succeed:
      new JwtService:
        override def createToken(user: User): Task[UserToken] =
          ZIO.succeed(UserToken(user.email, "bigAccess", Long.MaxValue))
        override def verifyToken(token: String): Task[UserId] =
          ZIO.succeed(UserId(1L, "daniel@rockthejvm.com"))
  end stubbedJwtService

  private def stubbedEmailService = ZLayer.succeed:
      new EmailService:
        override def sendEmail(to: String, subject: String, content: String): Task[Unit] =
          ZIO.unit
  end stubbedEmailService

  private def stubbedTokensRepo = ZLayer.succeed:
      new RecoveryTokensRepository:
        private val db = collection.mutable.Map[String, RecoveryToken](
          daniel.email -> RecoveryToken(daniel.email, "aToken", 999)
        )
        override def getToken(email: String): Task[Option[String]] =
          ZIO.succeed(db.get(email).map(_.token))
        override def checkToken(email: String, token: String): Task[Boolean] =
          ZIO.succeed(db.get(email).exists(_.token == token))
  end stubbedTokensRepo

  override def spec: Spec[TestEnvironment with Scope, Any] =
    suite("UserServiceSpec")(
      test("create and validate a user"):
          for
            service <- ZIO.service[UserService]
            created <- service.registerUser(daniel.email, "aPassword")
            valid   <- service.verifyPassword(created.email, "aPassword")
          yield assertTrue(valid)
      ,
      test("validate correct credentials"):
          for
            service <- ZIO.service[UserService]
            valid   <- service.verifyPassword(daniel.email, "aPassword")
            noUser  <- service.verifyPassword("nobody@mail.com", "aPassword")
            badPass <- service.verifyPassword(daniel.email, "wrong")
          yield assertTrue(valid && !noUser && !badPass)
      ,
      test("update password"):
          for
            service  <- ZIO.service[UserService]
            _        <- service.updatePassword(daniel.email, "aPassword", "scalaRulez")
            oldValid <- service.verifyPassword(daniel.email, "aPassword")
            newValid <- service.verifyPassword(daniel.email, "scalaRulez")
          yield assertTrue(newValid && !oldValid)
      ,
      test("delete user"):
          for
            service <- ZIO.service[UserService]
            noUser  <- service.deleteUser("nobody@mail.com", "aPassword").isSuccess
            badPass <- service.deleteUser(daniel.email, "wrong").isSuccess
            exists  <- service.deleteUser(daniel.email, "aPassword").isSuccess
            after   <- service.verifyPassword(daniel.email, "aPassword")
          yield assertTrue:
              !noUser && !badPass && exists && !after,
    ).provide(
      UserServiceLive.layer,
      stubbedUserRepo,
      stubbedJwtService,
      stubbedEmailService,
      stubbedTokensRepo
    )
