package com.rockthejvm.reviewboard.services

import com.rockthejvm.reviewboard.domain.data.*
import com.rockthejvm.reviewboard.http.requests.CreateCompanyRequest
import com.rockthejvm.reviewboard.repositories.CompanyRepository
import zio.*

/** Business logic for company listings
  */
trait CompanyService:
  /** Create a company from a CreateCompanyRequest
    * @param req
    *   the properties of the company to create
    * @return
    *   a task containing the created company
    */
  def create(req: CreateCompanyRequest): Task[Company]

  /** Get all companies
    * @return
    *   a task containing all found companies
    */
  def getAll: Task[List[Company]]

  /** Get all distinct filterable/searchable attributes of all companies
    * @return
    *   a task containing a populated CompanyFilter instance
    */
  def allFilters: Task[CompanyFilter]

  /** Search for all companies that match a given CompanyFilter criteria
    * @param filter
    *   the criteria to match companies against
    * @return
    *   a task containing all companies that match the criteria
    */
  def search(filter: CompanyFilter): Task[List[Company]]

  /** Get a company by id
    * @param id
    *   the id to search for
    * @return
    *   a task containing an option of the company with the given id
    */
  def getById(id: Long): Task[Option[Company]]

  /** Get a company by its slug attribute
    * @param slug
    *   the slug to search with
    * @return
    *   a task containing an option of the company with the given id
    */
  def getBySlug(slug: String): Task[Option[Company]]
end CompanyService

/** Implementation of CompanyService using a CompanyRepository layer
  * @param repo
  *   the repository instance to access data with
  */
class CompanyServiceLive private (repo: CompanyRepository) extends CompanyService:
  override def create(req: CreateCompanyRequest): Task[Company] =
    repo.create(req.toCompany(-1L))

  override def getAll: Task[List[Company]] =
    repo.getAll

  override def allFilters: Task[CompanyFilter] =
    repo.uniqueAttributes

  override def search(filter: CompanyFilter): Task[List[Company]] =
    repo.search(filter)

  override def getById(id: Long): Task[Option[Company]] =
    repo.getById(id)

  override def getBySlug(slug: String): Task[Option[Company]] =
    repo.getBySlug(slug)
end CompanyServiceLive

object CompanyServiceLive:
  val layer: ZLayer[CompanyRepository, Nothing, CompanyServiceLive] =
    ZLayer:
        ZIO.service[CompanyRepository].map(new CompanyServiceLive(_))
end CompanyServiceLive
