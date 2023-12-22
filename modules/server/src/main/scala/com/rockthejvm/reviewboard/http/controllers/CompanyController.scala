package com.rockthejvm.reviewboard.http.controllers

import com.rockthejvm.reviewboard.domain.data.Company

import collection.mutable
import com.rockthejvm.reviewboard.http.endpoints.CompanyEndpoints
import sttp.tapir.server.ServerEndpoint
import zio.*

/** a controller that implements handling logic for company endpoints */
class CompanyController private extends BaseController with CompanyEndpoints:
  // in-memory "database"
  private val db = mutable.Map[Long, Company](
    -1L -> Company(-1L, "invalid", "No company", "nothing.com")
  )

  val create: ServerEndpoint[Any, Task] =
    createEndpoint.serverLogicSuccess: req =>
      ZIO.succeed:
        val id = db.keys.max + 1
        val company = req.toCompany(id)
        db += (id -> company)
        company

  val getAll: ServerEndpoint[Any, Task] =
    getAllEndpoint.serverLogicSuccess: _ =>
      ZIO.succeed(db.values.toList)

  val getById: ServerEndpoint[Any, Task] =
    getByIdEndpoint.serverLogicSuccess: id =>
      ZIO.attempt(id.toLong).map(db.get)

  override val routes: List[ServerEndpoint[Any, Task]] =
    List(create, getAll, getById)

end CompanyController

object CompanyController:
  val makeZIO: UIO[CompanyController] =
    ZIO.succeed(new CompanyController)
