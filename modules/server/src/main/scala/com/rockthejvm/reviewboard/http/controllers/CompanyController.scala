package com.rockthejvm.reviewboard.http.controllers

import com.rockthejvm.reviewboard.http.endpoints.CompanyEndpoints
import com.rockthejvm.reviewboard.services.CompanyService
import sttp.tapir.server.ServerEndpoint
import zio.*

/** a controller that implements handling logic for company endpoints */
class CompanyController private (service: CompanyService) extends BaseController with CompanyEndpoints:
  val create: ServerEndpoint[Any, Task] =
    createEndpoint.serverLogicSuccess(service.create)
    
  val getAll: ServerEndpoint[Any, Task] =
    getAllEndpoint.serverLogicSuccess(_ => service.getAll)

  val getById: ServerEndpoint[Any, Task] =
    getByIdEndpoint.serverLogicSuccess: id =>
      ZIO.succeed(id.toLongOption).flatMap:
        case Some(value) => service.getById(value)
        case None => service.getBySlug(id)

  override val routes: List[ServerEndpoint[Any, Task]] =
    List(create, getAll, getById)

object CompanyController:
  val makeZIO: URIO[CompanyService, CompanyController] =
    ZIO.service[CompanyService]
      .map(service => new CompanyController(service))
