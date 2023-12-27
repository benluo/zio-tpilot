package com.rockthejvm.reviewboard.repositories

import com.rockthejvm.reviewboard.domain.data.Review
import zio.*
import io.getquill.*
import io.getquill.jdbczio.Quill

trait ReviewRepository extends BaseRepository[Review]:
  def getByCompanyId(id: Long): Task[List[Review]]
  def getByUserId(id: Long): Task[List[Review]]

class ReviewRepositoryLive private (quill: Quill.Postgres[SnakeCase]) extends ReviewRepository:
  import quill.*
  
  inline given schema: SchemaMeta[Review] =
    schemaMeta[Review]("reviews")
  inline given insMeta: InsertMeta[Review] =
    insertMeta[Review](_.id)
  inline given upMeta: UpdateMeta[Review] =
    updateMeta[Review](_.id)

  override def create(review: Review): Task[Review] =
    run:
      query[Review]
        .insertValue(lift(review))
        .returning(r => r)

  override def update(id: Long, op: Review => Review): Task[Review] =
    for
      curr <- getById(id).someOrFail(failMsg("update", id))
      updated <- run:
        query[Review]
          .filter(_.id == lift(id))
          .updateValue(lift(op(curr)))
          .returning(c => c)
    yield updated

  override def delete(id: Long): Task[Review] =
    for
      _ <- getById(id).someOrFail(failMsg("delete", id))
      deleted <- run:
        query[Review]
          .filter(_.id == lift(id))
          .delete
          .returning(c => c)
    yield deleted

  override def getById(id: Long): Task[Option[Review]] =
    run(query[Review].filter(_.id == lift(id)))
      .map(_.headOption)

  override def getAll: Task[List[Review]] =
    run(query[Review])

  private def failMsg(method: String, id: Long): Throwable =
    new RuntimeException(s"could not $method missing id: $id")

  override def getByCompanyId(id: Long): Task[List[Review]] =
    run(query[Review].filter(_.companyId == lift(id)))
  
  override def getByUserId(id: Long): Task[List[Review]] =
    run(query[Review].filter(_.userId == lift(id)))

object ReviewRepositoryLive:
  val layer: ZLayer[Quill.Postgres[SnakeCase], Nothing, ReviewRepositoryLive] =
    ZLayer:
      ZIO.service[Quill.Postgres[SnakeCase]]
        .map(new ReviewRepositoryLive(_))
