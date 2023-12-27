package com.rockthejvm.reviewboard.http.controllers

import com.rockthejvm.reviewboard.http.endpoints.ReviewEndpoints
import com.rockthejvm.reviewboard.services.ReviewService
import sttp.tapir.server.ServerEndpoint
import zio.*

class ReviewController private (service: ReviewService) extends BaseController with ReviewEndpoints:
  val create: ServerEndpoint[Any, Task] =
    createEndpoint.serverLogicSuccess(service.create)
    
  val getAll: ServerEndpoint[Any, Task] =
    getAllEndpoint.serverLogicSuccess(_ => service.getAll)
    
  val getById: ServerEndpoint[Any, Task] =
    getByIdEndpoint.serverLogicSuccess: id =>
      ZIO.attempt(id.toLong).flatMap(service.getById)
      
  val getByCompanyId: ServerEndpoint[Any, Task] =
    getByCompanyIdEndpoint.serverLogicSuccess: id =>
      ZIO.succeed(id.toLongOption).flatMap:
        case Some(value) => service.getByCompanyId(value)
        case None => ZIO.fail(new RuntimeException("company id does not exist"))
        
  val getByUserId: ServerEndpoint[Any, Task] =
    getByUserIdEndpoint.serverLogicSuccess: id =>
      ZIO.attempt(id.toLong).flatMap(service.getByUserId)
  
  override val routes: List[ServerEndpoint[Any, Task]] =
    List(create, getAll, getById, getByCompanyId, getByUserId)

object ReviewController:
  val makeZIO: URIO[ReviewService, ReviewController] =
    ZIO.service[ReviewService].map(ReviewController(_))
    