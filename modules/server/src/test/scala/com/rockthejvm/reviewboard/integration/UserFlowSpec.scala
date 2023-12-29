package com.rockthejvm.reviewboard.integration

import com.rockthejvm.reviewboard.config.{JwtConfig, RecoveryTokensConfig}
import com.rockthejvm.reviewboard.domain.data.UserToken
import zio.*
import com.rockthejvm.reviewboard.http.controllers.UserController
import com.rockthejvm.reviewboard.http.requests.{LoginRequest, RegisterUserRequest, UpdatePasswordRequest}
import com.rockthejvm.reviewboard.http.responses.UserResponse
import com.rockthejvm.reviewboard.repositories.{RecoveryTokensRepositoryLive, Repository, RepositorySpec, UserRepositoryLive}
import com.rockthejvm.reviewboard.services.*
import sttp.client3.*
import sttp.client3.testing.SttpBackendStub
import sttp.model.Method
import sttp.monad.MonadError
import sttp.tapir.server.stub.TapirStubInterpreter
import sttp.tapir.ztapir.RIOMonadError
import zio.test.*
import zio.json.*

object UserFlowSpec extends ZIOSpecDefault with RepositorySpec:
  override val initScript: String = "sql/integration.sql"

  private given zioMonadError: MonadError[Task] =
    new RIOMonadError[Any]

  private def backendStubZIO: URIO[JwtService & UserService, SttpBackend[Task, Nothing]] =
    for
      controller <- UserController.makeZIO
    yield TapirStubInterpreter(SttpBackendStub(MonadError[Task]))
      .whenServerEndpointsRunLogic(controller.routes)
      .backend()

  extension [A : JsonCodec](backend: SttpBackend[Task, Nothing])
    def sendReq[B : JsonCodec](
      method: Method,
      path: String,
      payload: A,
      maybeToken: Option[String] = None
    ): Task[Option[B]] =
      basicRequest
        .method(method, uri"$path")
        .body(payload.toJson)
        .auth.bearer(maybeToken.getOrElse(""))
        .send(backend)
        .map(_.body.toOption.flatMap(payload => payload.fromJson[B].toOption))

    def post[B : JsonCodec](
      path: String,
      payload: A,
      maybeToken: Option[String] = None
    ): Task[Option[B]] =
      sendReq(Method.POST, path, payload, maybeToken)

    def put[B : JsonCodec](
        path: String,
        payload: A,
        maybeToken: Option[String] = None
    ): Task[Option[B]] =
      sendReq(Method.PUT, path, payload, maybeToken)
  end extension

  private val regReq = RegisterUserRequest("daniel@rockthejvm.com", "aPassword")
  private val logReq = LoginRequest("daniel@rockthejvm.com", "aPassword")
  private val pReq = UpdatePasswordRequest("daniel@rockthejvm.com", "aPassword", "newOne")

  override def spec: Spec[TestEnvironment with Scope, Any] =
    suite("UserFlowSpec")(
      test("create user"):
        for
          backend  <- backendStubZIO
          response <- backend.post[UserResponse]("/users", regReq)
        yield assertTrue:
          response.contains(UserResponse("daniel@rockthejvm.com")),

      test("create and login"):
        for
          backend <- backendStubZIO
          _       <- backend.post[UserResponse]("/users", regReq)
          token   <- backend.post[UserToken]("/users/login", logReq)
        yield assertTrue:
          token.isDefined &&
          token.get.email == "daniel@rockthejvm.com",

      test("change password"):
        for
          backend <- backendStubZIO
          _       <- backend.post[UserResponse]("/users", regReq)
          token   <- backend.post[UserToken]("/users/login", logReq).someOrFail(new RuntimeException("auth failed"))
          _       <- backend.put[UserResponse]("/users/password", pReq, Some(token.token))
          oldTok  <- backend.post[UserToken]("/users/login", logReq)
          newTok  <- backend.post[UserToken]("/users/login", logReq.copy(password = pReq.newPassword))
        yield assertTrue:
          oldTok.isEmpty &&
          newTok.isDefined &&
          newTok.get.email == "daniel@rockthejvm.com",

    ).provide(
      Scope.default,
      UserServiceLive.layer,
      JwtServiceLive.layer,
      UserRepositoryLive.layer,
      Repository.quillLayer,
      dataSourceLayer,
      ZLayer.succeed:
        JwtConfig("secret", 3600),
      RecoveryTokensRepositoryLive.layer,
      ZLayer.succeed:
        RecoveryTokensConfig(3600L),
      ZLayer.succeed:
        new EmailService:
          override def sendEmail(to: String, subject: String, content: String): Task[Unit] = ZIO.unit
    )
