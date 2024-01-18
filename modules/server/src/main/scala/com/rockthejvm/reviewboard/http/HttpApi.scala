package com.rockthejvm.reviewboard.http

import com.rockthejvm.reviewboard.http.controllers.*
import com.rockthejvm.reviewboard.services.*
import sttp.tapir.server.ServerEndpoint
import zio.{Task, URIO}

/**
 * The api of the http layer of the application
 */
object HttpApi:
  private type R = ReviewService & CompanyService & UserService & JwtService
  
  /** a ZIO-wrapped list of all available endpoints */
  val endpointsZIO: URIO[R, List[ServerEndpoint[Any, Task]]] =
    makeControllers.map(_.flatMap(_.routes))

  private def makeControllers =
    for
      health    <- HealthController.makeZIO
      companies <- CompanyController.makeZIO
      reviews   <- ReviewController.makeZIO
      users     <- UserController.makeZIO
    yield List(health, companies, reviews, users)
