package com.rockthejvm.reviewboard.services

import com.auth0.jwt.{JWT, JWTVerifier}
import com.auth0.jwt.algorithms.Algorithm
import com.rockthejvm.reviewboard.config.{Configs, JwtConfig}
import com.rockthejvm.reviewboard.domain.data.{User, UserId, UserToken}
import zio.*

import java.time.Instant

trait JwtService:
  def createToken(user: User): Task[UserToken]
  def verifyToken(token: String): Task[UserId]

class JwtServiceLive private (jwtConfig: JwtConfig, clock: java.time.Clock) extends JwtService:
  private val ISSUER = "rockthejvm.com"
  private val algorithm = Algorithm.HMAC512(jwtConfig.secret)
  private val CLAIM_USERNAME = "username"

  private val verifier =
    JWT
      .require(algorithm)
      .withIssuer(ISSUER)
      .asInstanceOf[JWTVerifier.BaseVerification]
      .build(clock)

  override def createToken(user: User): Task[UserToken] =
    for
      now        <- ZIO.attempt(clock.instant())
      expiration <- ZIO.succeed(now.plusSeconds(jwtConfig.ttl))
      token      <- ZIO.attempt(makeJwt(user, now, expiration))
    yield UserToken(user.email, token, expiration.getEpochSecond)

  override def verifyToken(token: String): Task[UserId] =
    for
      decoded <- ZIO.attempt(verifier.verify(token))
      userId  <- ZIO.attempt:
        UserId(
          decoded.getSubject.toLong,
          decoded.getClaim(CLAIM_USERNAME).asString()
        )
    yield userId

  private def makeJwt(user: User, now: Instant, expiration: Instant): String =
    JWT
      .create()
      .withIssuer(ISSUER)
      .withIssuedAt(now)
      .withExpiresAt(expiration)
      .withSubject(user.id.toString)
      .withClaim(CLAIM_USERNAME, user.email)
      .sign(algorithm)

object JwtServiceLive:
  val layer: ZLayer[JwtConfig, Nothing, JwtServiceLive] = ZLayer:
    for
      config <- ZIO.service[JwtConfig]
      clock <- Clock.javaClock
    yield JwtServiceLive(config, clock)

  val configuredLayer: ZLayer[Any, Throwable, JwtServiceLive] =
    Configs.makeLayer[JwtConfig]("rockthejvm.jwt") >>> layer
