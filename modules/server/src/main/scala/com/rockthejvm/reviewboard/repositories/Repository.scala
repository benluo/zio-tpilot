package com.rockthejvm.reviewboard.repositories

import io.getquill.SnakeCase
import io.getquill.jdbczio.Quill
import zio.*

object Repository:
  private def quillLayer = Quill.Postgres.fromNamingStrategy(SnakeCase)
  private def dataSourceLayer = Quill.DataSource.fromPrefix("rockthejvm.db")

  val dataLayer: ZLayer[Any, Throwable, Quill.Postgres[SnakeCase.type]] =
    dataSourceLayer >>> quillLayer
end Repository
