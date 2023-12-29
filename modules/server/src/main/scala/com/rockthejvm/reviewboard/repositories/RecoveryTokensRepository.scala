package com.rockthejvm.reviewboard.repositories

import com.rockthejvm.reviewboard.config.{Configs, RecoveryTokensConfig}
import com.rockthejvm.reviewboard.domain.data.RecoveryToken
import zio.*
import io.getquill.*
import io.getquill.jdbczio.Quill

trait RecoveryTokensRepository:
  def getToken(email: String): Task[Option[String]]
  def checkToken(email: String, token: String): Task[Boolean]

class RecoveryTokensRepositoryLive private (
  tokensConfig: RecoveryTokensConfig,
  quill: Quill.Postgres[SnakeCase],
  userRepo: UserRepository
) extends RecoveryTokensRepository:
  import quill.*

  inline given schema: SchemaMeta[RecoveryToken] =
    schemaMeta[RecoveryToken]("recovery_tokens")
  inline given insMeta: InsertMeta[RecoveryToken] =
    insertMeta[RecoveryToken]()
  inline given upMeta: UpdateMeta[RecoveryToken] =
    updateMeta[RecoveryToken](_.email)

  private def makeFreshToken(email: String): Task[String] =
    findToken(email).flatMap:
      case Some(_) => replaceToken(email)
      case None => generateToken(email)

  private def findToken(email: String): Task[Option[String]] =
    run(query[RecoveryToken].filter(_.email == lift(email))).map(_.headOption.map(_.token))

  private def randomUppercaseString(len: Int): Task[String] =
    ZIO.succeed(scala.util.Random.alphanumeric.take(len).mkString.toUpperCase)

  private def replaceToken(email: String): Task[String] =
    for
      token <- randomUppercaseString(8)
      entry <- ZIO.attempt(RecoveryToken(email, token, java.lang.System.currentTimeMillis() + tokensConfig.duration))
      _     <- run(query[RecoveryToken].updateValue(lift(entry)))
    yield token

  private def generateToken(email: String): Task[String] =
    for
      token <- randomUppercaseString(8)
      entry <- ZIO.attempt(RecoveryToken(email, token, java.lang.System.currentTimeMillis() + tokensConfig.duration))
      _     <- run(query[RecoveryToken].insertValue(lift(entry)))
    yield token

  override def getToken(email: String): Task[Option[String]] =
    userRepo
      .getByEmail(email)
      .flatMap:
        case None => ZIO.none
        case Some(_) => makeFreshToken(email).map(Some(_))

  override def checkToken(email: String, token: String): Task[Boolean] =
    run(
      query[RecoveryToken].filter(row => row.email == lift(email) && row.token == lift(token))
    ).map(_.nonEmpty)

object RecoveryTokensRepositoryLive:
  private type R = UserRepository & Quill.Postgres[SnakeCase.type]
  val layer: ZLayer[R & RecoveryTokensConfig, Nothing, RecoveryTokensRepositoryLive] =
    ZLayer:
      for
        config   <- ZIO.service[RecoveryTokensConfig]
        quill    <- ZIO.service[Quill.Postgres[SnakeCase.type]]
        userRepo <- ZIO.service[UserRepository]
      yield RecoveryTokensRepositoryLive(config, quill, userRepo)

  val configuredLayer: ZLayer[R, Throwable, RecoveryTokensRepository] =
    Configs.makeLayer[RecoveryTokensConfig]("rockthejvm.recoverytokens") >>> layer
