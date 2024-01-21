package com.rockthejvm.reviewboard.http.controllers

import com.rockthejvm.reviewboard.domain.errors.ApplicationError
import com.rockthejvm.reviewboard.http.endpoints.HealthEndpoints
import sttp.tapir.*
import sttp.tapir.server.ServerEndpoint
import zio.*

/** a controller that implements health check endpoints */
class HealthController private extends Controller with HealthEndpoints:
  val health: ServerEndpoint[Any, Task] =
    healthEndpoint.serverLogicSuccess(_ => ZIO.succeed("All good!"))

  val error: ServerEndpoint[Any, Task] =
    errorEndpoint
      .serverLogic[Task](_ => ZIO.fail(ApplicationError("Boom!")).either)

  override val routes: List[ServerEndpoint[Any, Task]] = List(health, error)

object HealthController:
  val makeZIO: UIO[HealthController] =
    ZIO.succeed(new HealthController)
