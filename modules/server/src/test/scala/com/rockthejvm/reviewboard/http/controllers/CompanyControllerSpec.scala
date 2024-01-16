package com.rockthejvm.reviewboard.http.controllers

import com.rockthejvm.reviewboard.domain.data.{Company, CompanyFilter, User, UserId, UserToken}
import com.rockthejvm.reviewboard.http.requests.CreateCompanyRequest
import com.rockthejvm.reviewboard.services.{CompanyService, JwtService}
import com.rockthejvm.reviewboard.syntax.*
import sttp.client3.*
import sttp.client3.testing.SttpBackendStub
import sttp.monad.MonadError
import sttp.tapir.server.ServerEndpoint
import sttp.tapir.server.stub.TapirStubInterpreter
import sttp.tapir.ztapir.RIOMonadError
import zio.*
import zio.json.*
import zio.test.*

/** Specification for CompanyController */
object CompanyControllerSpec extends ZIOSpecDefault:
  override def spec: Spec[TestEnvironment with Scope, Any] =
    suite("CompanyControllerSpec")(
      test("post a new company"):
        val req =
          basicRequest
            .post(uri"/companies")
            .header("Authorization", "Bearer all_good")
            .body(CreateCompanyRequest("Rock the JVM", "rockthejvm.com").toJson)

        controller(_.create)(req)
          .assert("returns newly created company"): respBody =>
            respBody
              .toOption
              .flatMap(_.fromJson[Company].toOption)
              .contains(rtjvm),

      test("get all company listings"):
        controller(_.getAll)(basicRequest.get(uri"/companies"))
          .assert("returns list of all companies"): respBody =>
            respBody
              .toOption
              .flatMap(_.fromJson[List[Company]].toOption)
              .contains(List(rtjvm)),

      test("get a company by id - bad id"):
        controller(_.getById)(basicRequest.get(uri"/companies/10"))
          .assert("returns None if no such company exists"): respBody =>
            respBody
              .toOption
              .flatMap(_.fromJson[Company].toOption)
              .isEmpty,

      test("get a company by id - good id"):
        controller(_.getById)(basicRequest.get(uri"/companies/1"))
          .assert("returns company if exists"): respBody =>
            respBody
              .toOption
              .flatMap(_.fromJson[Company].toOption)
              .contains(rtjvm),

      test("get a company by slug - bad slug"):
        controller(_.getById)(basicRequest.get(uri"/companies/bogus"))
          .assert("returns None if no such company exists"): respBody =>
            respBody
              .toOption
              .flatMap(_.fromJson[Company].toOption)
              .isEmpty,

      test("get a company by slug - good slug"):
        controller(_.getById)(basicRequest.get(uri"/companies/rock-the-jvm"))
          .assert("returns None if no such company exists"): respBody =>
            respBody
              .toOption
              .flatMap(_.fromJson[Company].toOption)
              .contains(rtjvm),

    ).provide(serviceStub, jwtServiceStub)
  end spec

  private given zioMonadError: MonadError[Task] =
    new RIOMonadError[Any]

  private val rtjvm = Company(1L, "rock-the-jvm", "Rock the JVM", "rockthejvm.com")

  private val serviceStub =
    ZLayer.succeed:
      new CompanyService:
        override def create(req: CreateCompanyRequest): Task[Company] =
          ZIO.succeed(rtjvm)
        override def getAll: Task[List[Company]] =
          ZIO.succeed(List(rtjvm))
        override def getById(id: Long): Task[Option[Company]] =
          ZIO.succeed(Option.when(id == rtjvm.id)(rtjvm))
        override def getBySlug(slug: String): Task[Option[Company]] =
          ZIO.succeed(Option.when(slug == rtjvm.slug)(rtjvm))
        override def allFilters: Task[CompanyFilter] =
          ZIO.succeed(CompanyFilter.empty)
        override def search(filter: CompanyFilter): Task[List[Company]] =
          ZIO.succeed(List(rtjvm))
  end serviceStub

  private val jwtServiceStub =
    ZLayer.succeed:
      new JwtService:
        override def createToken(user: User): Task[UserToken] =
          ZIO.succeed(UserToken(user.email, "all_good", 999999L))
        override def verifyToken(token: String): Task[UserId] =
          ZIO.succeed(UserId(1L, "daniel@rockthejvm.com"))
  end jwtServiceStub

  private type ED = CompanyController => ServerEndpoint[Any, Task]
  private def backendStubZIO(f: ED): RIO[CompanyService & JwtService, SttpBackend[Task, Nothing]] =
    for
      controller <- CompanyController.makeZIO
      endpoint <- ZIO.succeed(f(controller))
    yield TapirStubInterpreter(SttpBackendStub(MonadError[Task]))
      .whenServerEndpointRunLogic(endpoint)
      .backend()


  private type Req = Request[Either[String, String], Any]
  private def controller(f: ED)(req: Req): RIO[CompanyService & JwtService, Either[String, String]] =
    for
      backendStub <- backendStubZIO(f)
      response <- req.send(backendStub)
    yield response.body

end CompanyControllerSpec
