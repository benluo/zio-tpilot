package com.rockthejvm.reviewboard

import com.rockthejvm.reviewboard.http.HttpApi
import com.rockthejvm.reviewboard.repositories.*
import com.rockthejvm.reviewboard.services.*
import sttp.tapir.*
import sttp.tapir.server.ziohttp.*
import zio.*
import zio.http.Server

/** the entry point for the application */
object Application extends ZIOAppDefault:
  private val serverProgram: RIO[Server with CompanyService, Unit] =
    for
      endpoints <- HttpApi.endpointsZIO
      interpreter <- ZIO.succeed(ZioHttpInterpreter(ZioHttpServerOptions.default))
      app <- ZIO.succeed(interpreter.toHttp(endpoints).withDefaultErrorResponse)
      _ <- Server.serve(app)
    yield ()

  override def run: Task[Unit] =
    serverProgram.provide(
      Server.default,
      // services
      CompanyServiceLive.layer,
      // repos
      CompanyRepositoryLive.layer,
      // other requirements
      Repository.dataLayer
    )
