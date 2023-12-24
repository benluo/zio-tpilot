package com.rockthejvm.reviewboard.http

import com.rockthejvm.reviewboard.http.controllers.*
import com.rockthejvm.reviewboard.services.CompanyService
import sttp.tapir.server.ServerEndpoint
import zio.{RIO, Task}

/** the api of the http layer of the application */
object HttpApi:
  /** a ZIO-wrapped list of all available endpoints */
  val endpointsZIO: RIO[CompanyService, List[ServerEndpoint[Any, Task]]] =
    makeControllers.map(gatherRoutes)
    
  private def gatherRoutes(controllers: List[BaseController]): List[ServerEndpoint[Any, Task]] =
    controllers.flatMap(_.routes)

  private def makeControllers: RIO[CompanyService, List[BaseController]] =
    for
      health    <- HealthController.makeZIO
      companies <- CompanyController.makeZIO
    yield List(health, companies)
