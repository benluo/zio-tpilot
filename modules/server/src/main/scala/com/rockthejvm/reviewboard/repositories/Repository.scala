package com.rockthejvm.reviewboard.repositories

import io.getquill.SnakeCase
import io.getquill.jdbczio.Quill
import zio.*

import javax.sql.DataSource

object Repository:
  def quillLayer: ZLayer[DataSource, Nothing, Quill.Postgres[SnakeCase.type]] =
    Quill.Postgres.fromNamingStrategy(SnakeCase)
    
  def dataSourceLayer: ZLayer[Any, Throwable, DataSource] =
    Quill.DataSource.fromPrefix("rockthejvm.db")

  val dataLayer: ZLayer[Any, Throwable, Quill.Postgres[SnakeCase.type]] =
    dataSourceLayer >>> quillLayer
