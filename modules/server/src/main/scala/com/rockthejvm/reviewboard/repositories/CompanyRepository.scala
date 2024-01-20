package com.rockthejvm.reviewboard.repositories

import com.rockthejvm.reviewboard.domain.data.{Company, CompanyFilter}
import zio.*
import io.getquill.*
import io.getquill.jdbczio.Quill

/** Data access layer for companies */
trait CompanyRepository extends Repository[Company]:
  /** Get a company by its slug attribute
    * @param slug
    *   the slug to search for
    * @return
    *   a task containing an option for the company with the given slug
    */
  def getBySlug(slug: String): Task[Option[Company]]

  /** Get companies that satisfy a search/filter criteria
    * @param filter
    *   the attributes to match against companies
    * @return
    *   a task containing the companies that match the filter arguments
    */
  def search(filter: CompanyFilter): Task[List[Company]]

  /** Get all unique/distinct attributes from companies in the table
    * @return
    *   a task containing a CompanyFilter instance populated with the attributes
    *   found
    */
  def uniqueAttributes: Task[CompanyFilter]
end CompanyRepository

/** Implementation of CompanyRepository using Quill and Postgres
  * @param quill
  *   the Quill instance to use to run queries
  */
class CompanyRepositoryLive private (quill: Quill.Postgres[SnakeCase])
    extends CompanyRepository:
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

  override def uniqueAttributes: Task[CompanyFilter] =
    for
      locations <- run(query[Company].map(_.location).distinct)
        .map(_.flatMap(_.toList))
      countries <- run(query[Company].map(_.country).distinct)
        .map(_.flatMap(_.toList))
      industries <- run(query[Company].map(_.industry).distinct)
        .map(_.flatMap(_.toList))
      tags <- run(query[Company].map(_.tags)).map(_.flatten.distinct)
    yield CompanyFilter(locations, countries, industries, tags)

  override def search(filter: CompanyFilter): Task[List[Company]] =
    if filter.isEmpty then getAll
    else
      run:
        query[Company]
          .filter: company =>
            liftQuery(filter.locations.toSet).contains(company.location) ||
              liftQuery(filter.countries.toSet).contains(company.country) ||
              liftQuery(filter.industries.toSet).contains(company.industry) ||
              sql"${company.tags} && ${lift(filter.tags)}".asCondition

  override def update(id: Long, op: Company => Company): Task[Company] =
    for
      curr <- getById(id).someOrFail(failMsg("update", id))
      updated <- run:
        query[Company]
          .filter(_.id == lift(id))
          .updateValue(lift(op(curr)))
          .returning(c => c)
    yield updated

  override def delete(id: Long): Task[Company] =
    for
      _ <- getById(id).someOrFail(failMsg("delete", id))
      deleted <- run:
        query[Company]
          .filter(_.id == lift(id))
          .delete
          .returning(c => c)
    yield deleted

  private def failMsg(method: String, id: Long): Throwable =
    new RuntimeException(s"could not $method missing id: $id")
end CompanyRepositoryLive

object CompanyRepositoryLive:
  val layer: ZLayer[Quill.Postgres[SnakeCase], Nothing, CompanyRepositoryLive] =
    ZLayer:
      ZIO
        .service[Quill.Postgres[SnakeCase]]
        .map(new CompanyRepositoryLive(_))
end CompanyRepositoryLive
