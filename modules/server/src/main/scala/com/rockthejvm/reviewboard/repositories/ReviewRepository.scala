package com.rockthejvm.reviewboard.repositories

import com.rockthejvm.reviewboard.domain.data.Review
import zio.*
import io.getquill.*
import io.getquill.jdbczio.Quill

/** Data access layer for reviews
  */
trait ReviewRepository extends Repository[Review]:
  /** Get reviews for a given company
    * @param id
    *   the company id to search with
    * @return
    *   a task containing all reviews for the given company
    */
  def getByCompanyId(id: Long): Task[List[Review]]

  /** Get reviews written by a given user
    * @param id
    *   the user id to search with
    * @return
    *   a task containing all reviews written by the given user
    */
  def getByUserId(id: Long): Task[List[Review]]
end ReviewRepository

/** Implementation of ReviewRepository with Quill and Postgres
  * @param quill
  *   the quill instance to run queries with
  */
class ReviewRepositoryLive private (quill: Quill.Postgres[SnakeCase]) extends ReviewRepository:
  import quill.*

  inline given schema: SchemaMeta[Review] =
    schemaMeta[Review]("reviews")
  inline given insMeta: InsertMeta[Review] =
    insertMeta[Review](_.id, _.created, _.updated)
  inline given upMeta: UpdateMeta[Review] =
    updateMeta[Review](_.id, _.companyId, _.userId, _.created)

  override def create(review: Review): Task[Review] =
    run(query[Review].insertValue(lift(review)).returning(r => r))

  override def getById(id: Long): Task[Option[Review]] =
    run(query[Review].filter(_.id == lift(id))).map(_.headOption)

  override def getByCompanyId(id: Long): Task[List[Review]] =
    run(query[Review].filter(_.companyId == lift(id)))

  override def getByUserId(id: Long): Task[List[Review]] =
    run(query[Review].filter(_.userId == lift(id)))

  override def getAll: Task[List[Review]] =
    run(query[Review])

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

  private def failMsg(method: String, id: Long): Throwable =
    new RuntimeException(s"could not $method missing id: $id")
end ReviewRepositoryLive

object ReviewRepositoryLive:
  val layer: ZLayer[Quill.Postgres[SnakeCase], Nothing, ReviewRepositoryLive] =
    ZLayer:
        ZIO
          .service[Quill.Postgres[SnakeCase]]
          .map(new ReviewRepositoryLive(_))
end ReviewRepositoryLive
