package com.rockthejvm.reviewboard.http.endpoints

import sttp.tapir.*

trait HealthEndpoints extends Endpoints:
  val healthEndpoint: EP[Unit, String] =
    baseEndpoint
      .tag("health")
      .name("health")
      .description("health check")
      .get
      .in("health")
      .out(plainBody[String])

  val errorEndpoint: EP[Unit, String] =
    baseEndpoint
      .tag("health")
      .name("errorHealth")
      .description("should fail")
      .get
      .in("health" / "error")
      .out(plainBody[String])
