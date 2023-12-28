package com.rockthejvm.reviewboard.http.controllers

import com.rockthejvm.reviewboard.http.endpoints.ReviewEndpoints
import com.rockthejvm.reviewboard.services.ReviewService
import sttp.tapir.server.ServerEndpoint
import zio.*

class ReviewController private (service: ReviewService) extends Controller with ReviewEndpoints:
  val create: ServerEndpoint[Any, Task] =
    createEndpoint.serverLogicSuccess(r => service.create(r, -1L)) // TODO: userID
    
  val getAll: ServerEndpoint[Any, Task] =
    getAllEndpoint.serverLogicSuccess(_ => service.getAll)
    
  val getById: ServerEndpoint[Any, Task] =
    getByIdEndpoint.serverLogicSuccess(service.getById)
      
  val getByCompanyId: ServerEndpoint[Any, Task] =
    getByCompanyIdEndpoint.serverLogicSuccess(service.getByCompanyId)
        
  val getByUserId: ServerEndpoint[Any, Task] =
    getByUserIdEndpoint.serverLogicSuccess(service.getByUserId)
  
  override val routes: List[ServerEndpoint[Any, Task]] =
    List(create, getAll, getById, getByCompanyId, getByUserId)

object ReviewController:
  val makeZIO: URIO[ReviewService, ReviewController] =
    ZIO.service[ReviewService].map(ReviewController(_))
    