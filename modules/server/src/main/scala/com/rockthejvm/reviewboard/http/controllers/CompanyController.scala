package com.rockthejvm.reviewboard.http.controllers

import com.rockthejvm.reviewboard.domain.data.UserId
import com.rockthejvm.reviewboard.http.endpoints.CompanyEndpoints
import com.rockthejvm.reviewboard.services.{CompanyService, JwtService}
import sttp.tapir.server.ServerEndpoint
import zio.*

/** a controller that implements handling logic for company endpoints */
class CompanyController private (
    service: CompanyService,
    jwtService: JwtService
) extends Controller
    with CompanyEndpoints:
  val create: ServerEndpoint[Any, Task] =
    createEndpoint
      .serverSecurityLogic[UserId, Task](jwtService.verifyToken(_).either)
      .serverLogic(_ => service.create(_).either)

  val getAll: ServerEndpoint[Any, Task] =
    getAllEndpoint.serverLogic(_ => service.getAll.either)

  val allFilters: ServerEndpoint[Any, Task] =
    allFiltersEndpoint.serverLogic(_ => service.allFilters.either)

  val search: ServerEndpoint[Any, Task] =
    searchEndpoint.serverLogic(service.search(_).either)

  val getById: ServerEndpoint[Any, Task] =
    getByIdEndpoint.serverLogic: id =>
      ZIO
        .succeed(id.toLongOption)
        .flatMap:
          case Some(value) => service.getById(value).either
          case None        => service.getBySlug(id).either

  override val routes: List[ServerEndpoint[Any, Task]] =
    List(create, getAll, allFilters, search, getById)

object CompanyController:
  val makeZIO: URIO[CompanyService & JwtService, CompanyController] =
    for
      companyService <- ZIO.service[CompanyService]
      jwtService     <- ZIO.service[JwtService]
    yield CompanyController(companyService, jwtService)
