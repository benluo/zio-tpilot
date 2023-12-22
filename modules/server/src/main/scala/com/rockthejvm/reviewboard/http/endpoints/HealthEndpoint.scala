package com.rockthejvm.reviewboard.http.endpoints

import sttp.tapir.*
import sttp.tapir.server.ServerEndpoint
import zio.*

trait HealthEndpoint:
  val healthEndpoint =
    endpoint
      .tag("health")
      .name("health")
      .description("health check")
      .get
      .in("health")
      .out(plainBody[String])
