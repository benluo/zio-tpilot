package com.rockthejvm.reviewboard.http.endpoints

import com.rockthejvm.reviewboard.domain.errors.HttpError
import sttp.tapir.*

trait Endpoints:
  protected type EP[I, O]       = Endpoint[Unit, I, Throwable, O, Any]
  protected type SecureEP[I, O] = Endpoint[String, I, Throwable, O, Any]

  val baseEndpoint: Endpoint[Unit, Unit, Throwable, Unit, Any] =
    endpoint
      .errorOut(statusCode and plainBody[String])
      .mapErrorOut[Throwable](HttpError.decode)(HttpError.encode)

  val secureEndpoint: Endpoint[String, Unit, Throwable, Unit, Any] =
    baseEndpoint
      .securityIn(auth.bearer[String]())
