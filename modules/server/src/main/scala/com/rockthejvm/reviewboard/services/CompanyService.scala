package com.rockthejvm.reviewboard.services

import com.rockthejvm.reviewboard.domain.data.*
import com.rockthejvm.reviewboard.http.requests.CreateCompanyRequest
import com.rockthejvm.reviewboard.repositories.CompanyRepository

import collection.mutable
import zio.*

/** business logic for company listings */
trait CompanyService:
  def create(req: CreateCompanyRequest): Task[Company]
  def getAll: Task[List[Company]]
  def getById(id: Long): Task[Option[Company]]
  def getBySlug(slug: String): Task[Option[Company]]

class CompanyServiceLive private (repo: CompanyRepository) extends CompanyService:
  override def create(req: CreateCompanyRequest): Task[Company] =
    repo.create(req.toCompany(-1L))

  override def getAll: Task[List[Company]] =
    repo.getAll

  override def getById(id: Long): Task[Option[Company]] =
    repo.getById(id)

  override def getBySlug(slug: String): Task[Option[Company]] =
    repo.getBySlug(slug)
end CompanyServiceLive

object CompanyServiceLive:
  val layer: ZLayer[CompanyRepository, Nothing, CompanyServiceLive] =
    ZLayer:
      ZIO.service[CompanyRepository].map(new CompanyServiceLive(_))
