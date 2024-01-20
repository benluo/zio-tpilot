package com.rockthejvm.reviewboard.http.controllers

import com.rockthejvm.reviewboard.http.endpoints.ReviewEndpoints
import com.rockthejvm.reviewboard.services.{JwtService, ReviewService}
import sttp.tapir.server.ServerEndpoint
import zio.*

class ReviewController private (
    reviewService: ReviewService,
    jwtService: JwtService
) extends Controller
    with ReviewEndpoints:
  val create: ServerEndpoint[Any, Task] =
    createEndpoint
      .serverSecurityLogic(jwtService.verifyToken(_).either)
      .serverLogic: userId =>
        reviewReq =>
          reviewService
            .create(reviewReq, userId.id)
            .either

  val getAll: ServerEndpoint[Any, Task] =
    getAllEndpoint.serverLogic(_ => reviewService.getAll.either)

  val getById: ServerEndpoint[Any, Task] =
    getByIdEndpoint.serverLogic(reviewService.getById(_).either)

  val getByCompanyId: ServerEndpoint[Any, Task] =
    getByCompanyIdEndpoint.serverLogic(reviewService.getByCompanyId(_).either)

  val getByUserId: ServerEndpoint[Any, Task] =
    getByUserIdEndpoint.serverLogic(reviewService.getByUserId(_).either)

  override val routes: List[ServerEndpoint[Any, Task]] =
    List(create, getAll, getById, getByCompanyId, getByUserId)

object ReviewController:
  val makeZIO: URIO[ReviewService & JwtService, ReviewController] =
    for
      reviewService <- ZIO.service[ReviewService]
      jwtService    <- ZIO.service[JwtService]
    yield ReviewController(reviewService, jwtService)
