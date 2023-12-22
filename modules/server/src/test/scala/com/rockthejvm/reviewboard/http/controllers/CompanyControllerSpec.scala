package com.rockthejvm.reviewboard.http.controllers

import com.rockthejvm.reviewboard.domain.data.Company
import com.rockthejvm.reviewboard.http.requests.CreateCompanyRequest
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
        val program = testEndpoint(_.create):
          basicRequest
            .post(uri"/companies")
            .body(CreateCompanyRequest("Rock the JVM", "rockthejvm.com").toJson)

        program.assert("returns newly created company"): respBody =>
          respBody
            .toOption
            .flatMap(_.fromJson[Company].toOption)
            .contains(Company(1, "rock-the-jvm", "Rock the JVM", "rockthejvm.com")),

      test("get all company listings"):
        val program = testEndpoint(_.getAll):
          basicRequest.get(uri"/companies")

        program.assert("returns list of all companies"): respBody =>
          respBody
            .toOption
            .flatMap(_.fromJson[List[Company]].toOption)
            .contains(List.empty),

      test("get a company by id"):
        val program = testEndpoint(_.getById):
          basicRequest.get(uri"/companies/1")

        program.assert("returns None if no such company exists"): respBody =>
          respBody.toOption
            .flatMap(_.fromJson[Company].toOption)
            .isEmpty,
    )
  end spec

  private given zioMonadError: MonadError[Task] =
    new RIOMonadError[Any]

  private type ED = CompanyController => ServerEndpoint[Any, Task]
  private def backendStubZIO(f: ED): Task[SttpBackend[Task, Nothing]] =
    for
      controller <- CompanyController.makeZIO
      endpoint <- ZIO.succeed(f(controller))
    yield TapirStubInterpreter(SttpBackendStub(MonadError[Task]))
      .whenServerEndpointRunLogic(endpoint)
      .backend()


  private type Req = Request[Either[String, String], Any]
  private def testEndpoint(f: ED)(req: Req): Task[Either[String, String]] =
    for
      backendStub <- backendStubZIO(f)
      response <- req.send(backendStub)
    yield response.body

end CompanyControllerSpec
