package com.rockthejvm.reviewboard.http.controllers

import com.rockthejvm.reviewboard.http.endpoints.ReviewEndpoints
import com.rockthejvm.reviewboard.services.ReviewService
import sttp.tapir.server.ServerEndpoint
import zio.*

class ReviewController private (service: ReviewService) extends Controller with ReviewEndpoints:
  val create: ServerEndpoint[Any, Task] =
    createEndpoint.serverLogic(r => service.create(r, -1L).either) // TODO: userID
    
  val getAll: ServerEndpoint[Any, Task] =
    getAllEndpoint.serverLogic(_ => service.getAll.either)
    
  val getById: ServerEndpoint[Any, Task] =
    getByIdEndpoint.serverLogic(service.getById(_).either)
      
  val getByCompanyId: ServerEndpoint[Any, Task] =
    getByCompanyIdEndpoint.serverLogic(service.getByCompanyId(_).either)
        
  val getByUserId: ServerEndpoint[Any, Task] =
    getByUserIdEndpoint.serverLogic(service.getByUserId(_).either)
  
  override val routes: List[ServerEndpoint[Any, Task]] =
    List(create, getAll, getById, getByCompanyId, getByUserId)

object ReviewController:
  val makeZIO: URIO[ReviewService, ReviewController] =
    ZIO.service[ReviewService].map(ReviewController(_))
    