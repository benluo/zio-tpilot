package com.rockthejvm.reviewboard.http.controllers

import com.rockthejvm.reviewboard.syntax.*

import sttp.client3.*
import sttp.client3.testing.SttpBackendStub
import sttp.monad.MonadError
import sttp.tapir.server.stub.TapirStubInterpreter
import sttp.tapir.ztapir.RIOMonadError

import zio.*
import zio.test.*

/** Specification for HealthController */
object HealthControllerSpec extends ZIOSpecDefault:
  override def spec: Spec[TestEnvironment with Scope, Any] =
    suite("HealthControllerSpec")(
      test("health check"):
        val program = for
          controller <- HealthController.makeZIO
          endpoint   <- ZIO.succeed(controller.health)
          backendStub <- ZIO.succeed:
            TapirStubInterpreter(SttpBackendStub(MonadError[Task]))
              .whenServerEndpointRunLogic(endpoint)
              .backend()
          response <- basicRequest.get(uri"/health").send(backendStub)
        yield response.body

        program.assert("works")(_.toOption.contains("All good!"))
    )
  end spec

  private given zioMonadError: MonadError[Task] =
    new RIOMonadError[Any]
end HealthControllerSpec
