package com.rockthejvm.reviewboard.services

import com.rockthejvm.reviewboard.domain.data.*
import com.rockthejvm.reviewboard.http.requests.CreateCompanyRequest

import collection.mutable
import zio.*

/** business logic for company listings */
trait CompanyService:
  def create(req: CreateCompanyRequest): Task[Company]
  def getAll: Task[List[Company]]
  def getById(id: Long): Task[Option[Company]]
  def getBySlug(slug: String): Task[Option[Company]]

object CompanyService:
  val dummyLayer: ULayer[CompanyServiceDummy] =
    ZLayer.succeed(new CompanyServiceDummy)

class CompanyServiceDummy extends CompanyService:
  private val db = mutable.Map.empty[Long, Company]

  override def create(req: CreateCompanyRequest): Task[Company] =
    ZIO.succeed:
      val id = db.keys.maxOption.getOrElse(0L) + 1L
      val company = req.toCompany(id)
      db += (id -> company)
      company

  override def getAll: Task[List[Company]] =
    ZIO.succeed(db.values.toList)

  override def getById(id: Long): Task[Option[Company]] =
    ZIO.succeed(db.get(id))

  override def getBySlug(slug: String): Task[Option[Company]] =
    ZIO.succeed(db.values.find(_.slug == slug))
end CompanyServiceDummy
