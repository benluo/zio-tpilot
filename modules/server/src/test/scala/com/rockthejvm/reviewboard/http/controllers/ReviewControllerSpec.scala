package com.rockthejvm.reviewboard.http.controllers

import com.rockthejvm.reviewboard.domain.data.{Review, User, UserId, UserToken}
import com.rockthejvm.reviewboard.http.requests.CreateReviewRequest
import com.rockthejvm.reviewboard.services.{JwtService, ReviewService}
import sttp.client3.*
import sttp.client3.testing.SttpBackendStub
import sttp.monad.MonadError
import sttp.tapir.server.ServerEndpoint
import sttp.tapir.server.stub.TapirStubInterpreter
import sttp.tapir.ztapir.RIOMonadError
import zio.*
import zio.json.*
import zio.test.*

import java.time.Instant

object ReviewControllerSpec extends ZIOSpecDefault:
  private given zioMonadError: MonadError[Task] =
    new RIOMonadError[Any]

  private val goodReview =
    Review(1L, 1L, 1L, 5, 5, 5, 5, 10, "all good", Instant.now(), Instant.now())

  private val serviceStub =
    ZLayer.succeed:
      new ReviewService:
        override def create(
            req: CreateReviewRequest,
            userId: Long
        ): Task[Review] =
          ZIO.succeed(goodReview)
        override def getAll: Task[List[Review]] =
          ZIO.succeed(List(goodReview))
        override def getById(id: Long): Task[Option[Review]] =
          ZIO.succeed(Some(goodReview))
        override def getByCompanyId(id: Long): Task[List[Review]] =
          ZIO.succeed(List(goodReview))
        override def getByUserId(id: Long): Task[List[Review]] =
          ZIO.succeed(List(goodReview))
  end serviceStub

  private val jwtServiceStub =
    ZLayer.succeed:
      new JwtService:
        override def createToken(user: User): Task[UserToken] =
          ZIO.succeed(UserToken(user.email, "all_good", 999999L))
        override def verifyToken(token: String): Task[UserId] =
          ZIO.succeed(UserId(1L, "daniel@rockthejvm.com"))
  end jwtServiceStub

  private type ControllerMethod = ReviewController => ServerEndpoint[Any, Task]
  private type StubbedBackend =
    RIO[ReviewService & JwtService, SttpBackend[Task, Nothing]]
  private def backendStubZIO(f: ControllerMethod): StubbedBackend =
    for
      controller <- ReviewController.makeZIO
      endpoint   <- ZIO.succeed(f(controller))
    yield TapirStubInterpreter(SttpBackendStub(MonadError[Task]))
      .whenServerEndpointRunLogic(endpoint)
      .backend()

  override def spec: Spec[TestEnvironment with Scope, Any] =
    suite("ReviewControllerSpec")(
      test("create"):
        for
          stub <- backendStubZIO(_.create)
          request <- ZIO.succeed:
            basicRequest
              .post(uri"/reviews")
              .header("Authorization", "Bearer all_good")
              .body(CreateReviewRequest(1L, 5, 5, 5, 5, 10, "all good").toJson)
          response <- request.send(stub)
        yield assertTrue:
          response.body.toOption
            .flatMap(_.fromJson[Review].toOption)
            .contains(goodReview)
      ,
      test("getById"):
        for
          stub     <- backendStubZIO(_.getById)
          request  <- ZIO.succeed(basicRequest.get(uri"/reviews/1"))
          response <- request.send(stub)
        yield assertTrue:
          response.body.toOption
            .flatMap(_.fromJson[Option[Review]].toOption)
            .contains(Some(goodReview))
      ,
      test("getByCompanyId"):
        for
          stub     <- backendStubZIO(_.getByCompanyId)
          request  <- ZIO.succeed(basicRequest.get(uri"/reviews/company/1"))
          response <- request.send(stub)
        yield assertTrue:
          response.body.toOption
            .flatMap(_.fromJson[List[Review]].toOption)
            .contains(List(goodReview))
      ,
      test("getByUserId"):
        for
          stub     <- backendStubZIO(_.getByUserId)
          request  <- ZIO.succeed(basicRequest.get(uri"/reviews/user/1"))
          response <- request.send(stub)
        yield assertTrue:
          response.body.toOption
            .flatMap(_.fromJson[List[Review]].toOption)
            .contains(List(goodReview))
      ,
      test("getAll"):
        for
          stub     <- backendStubZIO(_.getAll)
          request  <- ZIO.succeed(basicRequest.get(uri"/reviews"))
          response <- request.send(stub)
        yield assertTrue:
          response.body.toOption
            .flatMap(_.fromJson[List[Review]].toOption)
            .contains(List(goodReview)),
    ).provide(serviceStub, jwtServiceStub)
