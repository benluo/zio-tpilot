package com.rockthejvm.reviewboard.http.controllers

import com.rockthejvm.reviewboard.http.endpoints.CompanyEndpoints
import com.rockthejvm.reviewboard.services.CompanyService
import sttp.tapir.server.ServerEndpoint
import zio.*

/** a controller that implements handling logic for company endpoints */
class CompanyController private (service: CompanyService) extends Controller with CompanyEndpoints:
  val create: ServerEndpoint[Any, Task] =
    createEndpoint.serverLogic(service.create(_).either)
    
  val getAll: ServerEndpoint[Any, Task] =
    getAllEndpoint.serverLogic(_ => service.getAll.either)

  val getById: ServerEndpoint[Any, Task] =
    getByIdEndpoint.serverLogic: id =>
      ZIO.succeed(id.toLongOption).flatMap:
        case Some(value) => service.getById(value).either
        case None => service.getBySlug(id).either

  override val routes: List[ServerEndpoint[Any, Task]] =
    List(create, getAll, getById)

object CompanyController:
  val makeZIO: URIO[CompanyService, CompanyController] =
    ZIO.service[CompanyService].map(new CompanyController(_))
