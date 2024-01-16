package com.rockthejvm.reviewboard.services

import collection.mutable
import com.rockthejvm.reviewboard.domain.data.{Company, CompanyFilter}
import com.rockthejvm.reviewboard.http.requests.CreateCompanyRequest
import com.rockthejvm.reviewboard.repositories.CompanyRepository
import com.rockthejvm.reviewboard.syntax.*
import zio.*
import zio.test.*

object CompanyServiceSpec extends ZIOSpecDefault:
  override def spec: Spec[TestEnvironment with Scope, Any] =
    suite("CompanyServiceTest")(
      test("create"):
        service(_.create(CreateCompanyRequest("Rock the JVM", "rockthejvm.com")))
          .assert("initializes company correctly"): company =>
            company.name == "Rock the JVM" &&
            company.url == "rockthejvm.com" &&
            company.slug == "rock-the-jvm",

      test("get by id - bad id"):
        service(_.getById(-1L))
          .assert("returns None")(_.isEmpty),

      test("get by id - good id"):
        val program = for
          created <- service(_.create(CreateCompanyRequest("Rock the JVM", "rockthjvm.com")))
          gotten <- service(_.getById(created.id))
        yield (created, gotten)

        program.assert("returns correct company"):
          case (c, Some(g)) => c == g
          case _ => false,

      test("get by slug - bad slug"):
        service(_.getBySlug("bogus"))
          .assert("returns None")(_.isEmpty),

      test("get by slug - good slug"):
        val program = for
          created <- service(_.create(CreateCompanyRequest("Rock the JVM", "rockthjvm.com")))
          gotten <- service(_.getBySlug(created.slug))
        yield (created, gotten)

        program.assert("returns correct company"):
          case (c, Some(g)) => c == g
          case _ => false,

      test("get all - empty"):
        service(_.getAll).assert("returns empty list")(_.isEmpty),

      test("get all - with companies"):
        val program = for
          c1 <- service(_.create(CreateCompanyRequest("Rock the JVM", "rockthejvm.com")))
          c2 <- service(_.create(CreateCompanyRequest("Clock the JVM", "clockthejvm.com")))
          all <- service(_.getAll)
        yield (c1, c2, all)

        program.assert("returns all companies"):
          case (c1, c2, all) => all.toSet == Set(c1, c2),

    ).provide(
      CompanyServiceLive.layer,
      stubRepoLayer
    )
  end spec

  private val service = ZIO.serviceWithZIO[CompanyService]

  private val stubRepoLayer = ZLayer.succeed(
    new CompanyRepository:
      val db: mutable.Map[Long, Company] = mutable.Map.empty

      override def create(company: Company): Task[Company] =
        ZIO.succeed:
          val newId = db.keys.maxOption.getOrElse(0L) + 1
          val newCompany = company.copy(id = newId)
          db += (newId -> newCompany)
          newCompany

      override def update(id: Long, op: Company => Company): Task[Company] =
        ZIO.attempt:
          val company = db(id)
          val updated = op(company)
          db += (id -> updated)
          updated

      override def delete(id: Long): Task[Company] =
        ZIO.attempt:
          val company = db(id)
          db -= id
          company

      override def getById(id: Long): Task[Option[Company]] =
        ZIO.succeed(db.get(id))

      override def getBySlug(slug: String): Task[Option[Company]] =
        ZIO.succeed(db.values.find(_.slug == slug))

      override def getAll: Task[List[Company]] =
        ZIO.succeed(db.values.toList)

      override def uniqueAttributes: Task[CompanyFilter] =
        ZIO.succeed(CompanyFilter.empty)

      override def search(filter: CompanyFilter): Task[List[Company]] =
        ZIO.succeed(db.values.toList)
  )
end CompanyServiceSpec
