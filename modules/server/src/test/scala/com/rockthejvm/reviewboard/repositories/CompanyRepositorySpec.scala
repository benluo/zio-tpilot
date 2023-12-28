package com.rockthejvm.reviewboard.repositories

import com.rockthejvm.reviewboard.domain.data.Company
import zio.*
import zio.test.*

import java.sql.SQLException

object CompanyRepositorySpec extends ZIOSpecDefault with RepositorySpec:
  override val initScript: String = "sql/companies.sql"

  private val rtjvm = Company(-1L, "rock-the-jvm", "Rock the JVM", "rockthejvm.com")

  override def spec: Spec[TestEnvironment with Scope, Any] =
    suite("CompanyRepositorySpec")(
      test("create"):
        for
          repo    <- ZIO.service[CompanyRepository]
          company <- repo.create(rtjvm)
        yield assertTrue(
          company match
            case Company(_, "rock-the-jvm", "Rock the JVM", "rockthejvm.com", _, _, _, _, _) => true
            case _ => false
        ),

      test("create duplicate - error"):
        for
          repo    <- ZIO.service[CompanyRepository]
          company <- repo.create(rtjvm)
          err     <- repo.create(company).flip
        yield assertTrue(err.isInstanceOf[SQLException]),

      test("getById - bad id"):
        for
          repo    <- ZIO.service[CompanyRepository]
          company <- repo.getById(-1L)
        yield assertTrue(company.isEmpty),

      test("getById - good id"):
        for
          repo    <- ZIO.service[CompanyRepository]
          company <- repo.create(rtjvm)
          gotten  <- repo.getById(company.id)
        yield assertTrue(gotten.contains(company)),

      test("getBySlug - bad slug"):
        for
          repo    <- ZIO.service[CompanyRepository]
          company <- repo.getBySlug("bogus")
        yield assertTrue(company.isEmpty),

      test("getBySlug - good slug"):
        for
          repo    <- ZIO.service[CompanyRepository]
          company <- repo.create(rtjvm)
          gotten  <- repo.getBySlug(company.slug)
        yield assertTrue(gotten.contains(company)),

      test("getAll - empty"):
        for
          repo      <- ZIO.service[CompanyRepository]
          companies <- repo.getAll
        yield assertTrue(companies.isEmpty),

      test("getAll - full"):
        for
          repo   <- ZIO.service[CompanyRepository]
          comp1  <- repo.create(rtjvm)
          comp2  <- repo.create(Company(-1L, "clock-the-jvm", "Clock the JVM", "clockthejvm.com"))
          gotten <- repo.getAll
        yield assertTrue:
          gotten.toSet == Set(comp1, comp2),

      test("update record - bad id"):
        for
          repo <- ZIO.service[CompanyRepository]
          err  <- repo.update(10L, _.copy(slug = "boo")).flip
        yield assertTrue:
          err.getMessage.contains(s"could not update missing id: 10"),

      test("update record - valid change"):
        for
          repo    <- ZIO.service[CompanyRepository]
          company <- repo.create(rtjvm)
          updated <- repo.update(company.id, _.copy(url = "blog.rockthejvm.com"))
          fetched <- repo.getById(company.id)
        yield assertTrue(fetched.contains(updated)),

      test("delete record - bad id"):
        for
          repo <- ZIO.service[CompanyRepository]
          err  <- repo.delete(99L).flip
        yield assertTrue:
          err.getMessage.contains(s"could not delete missing id: 99"),

      test("delete record - valid id"):
        for
          repo    <- ZIO.service[CompanyRepository]
          company <- repo.create(Company(-1L, "rock-the-jvm", "Rock the JVM", "rockthejvm.com"))
          _       <- repo.delete(company.id)
          fetched <- repo.getById(company.id)
        yield assertTrue(fetched.isEmpty),

    ).provide(
      CompanyRepositoryLive.layer,
      dataSourceLayer,
      Repository.quillLayer,
      Scope.default
    )
  end spec
end CompanyRepositorySpec
