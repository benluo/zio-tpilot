package com.rockthejvm.reviewboard.services

import com.rockthejvm.reviewboard.config.JwtConfig
import com.rockthejvm.reviewboard.domain.data.User
import zio.*
import zio.test.*

object JwtServiceSpec extends ZIOSpecDefault:
  override def spec: Spec[TestEnvironment with Scope, Any] =
    suite("JwtServiceSpec")(
      test("create and validate token"):
          for
            service   <- ZIO.service[JwtService]
            userToken <- service.createToken(User(1L, "daniel@rockthejvm.com", "unimportant"))
            userId    <- service.verifyToken(userToken.token)
          yield assertTrue:
              userId.id == 1L &&
                userId.email == "daniel@rockthejvm.com",
    ).provide(
      JwtServiceLive.layer,
      ZLayer.succeed(JwtConfig("secret", 3600))
    )
