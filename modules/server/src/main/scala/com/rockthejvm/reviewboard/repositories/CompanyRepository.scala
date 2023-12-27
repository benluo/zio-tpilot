package com.rockthejvm.reviewboard.repositories

import com.rockthejvm.reviewboard.domain.data.Company
import zio.*
import io.getquill.*
import io.getquill.jdbczio.Quill

/** db logic for company listings */
trait CompanyRepository extends BaseRepository[Company]:
  def getBySlug(slug: String): Task[Option[Company]]

/** implementation of CompanyRepository using quill and postgresql */
class CompanyRepositoryLive private (quill: Quill.Postgres[SnakeCase]) extends CompanyRepository:
  import quill.*

  inline given schema: SchemaMeta[Company] =
    schemaMeta[Company]("companies")
  inline given insMeta: InsertMeta[Company] =
    insertMeta[Company](_.id)
  inline given upMeta: UpdateMeta[Company] =
    updateMeta[Company](_.id)

  override def create(company: Company): Task[Company] =
    run:
      query[Company]
        .insertValue(lift(company))
        .returning(c => c)

  override def getById(id: Long): Task[Option[Company]] =
    run(query[Company].filter(_.id == lift(id)))
      .map(_.headOption)

  override def getBySlug(slug: String): Task[Option[Company]] =
    run(query[Company].filter(_.slug == lift(slug)))
      .map(_.headOption)

  override def getAll: Task[List[Company]] =
    run(query[Company])

  override def update(id: Long, op: Company => Company): Task[Company] =
    for
      curr    <- getById(id).someOrFail(failMsg("update", id))
      updated <- run:
        query[Company]
          .filter(_.id == lift(id))
          .updateValue(lift(op(curr)))
          .returning(c => c)
    yield updated

  override def delete(id: Long): Task[Company] =
    for
      _       <- getById(id).someOrFail(failMsg("delete", id))
      deleted <- run:
        query[Company]
          .filter(_.id == lift(id))
          .delete
          .returning(c => c)
    yield deleted
    
  private def failMsg(method: String, id: Long): Throwable =
    new RuntimeException(s"could not $method missing id: $id")

object CompanyRepositoryLive:
  val layer: ZLayer[Quill.Postgres[SnakeCase], Nothing, CompanyRepositoryLive] =
    ZLayer:
      ZIO.service[Quill.Postgres[SnakeCase]]
        .map(new CompanyRepositoryLive(_))
