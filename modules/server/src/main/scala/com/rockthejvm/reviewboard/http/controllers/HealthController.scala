package com.rockthejvm.reviewboard.http.controllers

import com.rockthejvm.reviewboard.http.endpoints.HealthEndpoint
import sttp.tapir.server.ServerEndpoint
import zio.*

/** a controller that implements health check endpoints */
class HealthController private extends BaseController with HealthEndpoint:
  val health: ServerEndpoint[Any, Task] =
    healthEndpoint
      .serverLogicSuccess[Task](_ => ZIO.succeed("All good!"))

  override val routes: List[ServerEndpoint[Any, Task]] =
    List(health)
end HealthController

object HealthController:
  val makeZIO: UIO[HealthController] =
    ZIO.succeed(new HealthController)
