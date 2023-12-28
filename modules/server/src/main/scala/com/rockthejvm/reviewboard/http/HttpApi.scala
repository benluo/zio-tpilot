package com.rockthejvm.reviewboard.http

import com.rockthejvm.reviewboard.http.controllers.*
import com.rockthejvm.reviewboard.services.{CompanyService, ReviewService}
import sttp.tapir.server.ServerEndpoint
import zio.{URIO, Task, ZIO}

/** the api of the http layer of the application */
object HttpApi:
  /** a ZIO-wrapped list of all available endpoints */
  val endpointsZIO: URIO[ReviewService with CompanyService, List[ServerEndpoint[Any, Task]]] =
    makeControllers.map(gatherRoutes)
    
  private def gatherRoutes(controllers: List[Controller]) =
    controllers.flatMap(_.routes)

  private def makeControllers =
    for
      health    <- HealthController.makeZIO
      companies <- CompanyController.makeZIO
      reviews   <- ReviewController.makeZIO
    yield List(health, companies, reviews)
