package com.rockthejvm.reviewboard.repositories

import io.getquill.SnakeCase
import io.getquill.jdbczio.Quill
import zio.*

import javax.sql.DataSource

trait Repository[A]:
  def create(item: A): Task[A]
  def update(id: Long, op: A => A): Task[A]
  def delete(id: Long): Task[A]
  def getById(id: Long): Task[Option[A]]
  def getAll: Task[List[A]]

object Repository:
  def quillLayer: ZLayer[DataSource, Nothing, Quill.Postgres[SnakeCase.type]] =
    Quill.Postgres.fromNamingStrategy(SnakeCase)
    
  def dataSourceLayer: ZLayer[Any, Throwable, DataSource] =
    Quill.DataSource.fromPrefix("rockthejvm.db")

  val dataLayer: ZLayer[Any, Throwable, Quill.Postgres[SnakeCase.type]] =
    dataSourceLayer >>> quillLayer
