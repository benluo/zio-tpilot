package com.rockthejvm.reviewboard.repositories

import org.testcontainers.containers.PostgreSQLContainer
import com.rockthejvm.reviewboard.domain.data.Company
import com.rockthejvm.reviewboard.syntax.*
import org.postgresql.ds.PGSimpleDataSource
import zio.*
import zio.test.*

import java.sql.SQLException
import javax.sql.DataSource

object CompanyRepositorySpec extends ZIOSpecDefault:
  override def spec: Spec[TestEnvironment with Scope, Any] =
    suite("CompanyRepositorySpec")(
      test("create"):
        val program = for
          repo    <- ZIO.service[CompanyRepository]
          company <- repo.create(rtjvm)
        yield company

        program.assert():
          case Company(_, "rock-the-jvm", "Rock the JVM", "rockthejvm.com", _, _, _, _, _) => true
          case _ => false,

      test("create duplicate - error"):
        val program = for
          repo    <- ZIO.service[CompanyRepository]
          company <- repo.create(rtjvm)
          err     <- repo.create(company).flip
        yield err

        program.assert()(_.isInstanceOf[SQLException]),

      test("getById - bad id"):
        val program = for
          repo    <- ZIO.service[CompanyRepository]
          company <- repo.getById(-1L)
        yield company

        program.assert()(_.isEmpty),

      test("getById - good id"):
        val program = for
          repo    <- ZIO.service[CompanyRepository]
          company <- repo.create(rtjvm)
          gotten  <- repo.getById(company.id)
        yield (company, gotten)

        program.assert():
          case (c, Some(g)) => c == g
          case _ => false,

      test("getBySlug - bad slug"):
        val program = for
          repo    <- ZIO.service[CompanyRepository]
          company <- repo.getBySlug("bogus")
        yield company

        program.assert()(_.isEmpty),

      test("getBySlug - good slug"):
        val program = for
          repo    <- ZIO.service[CompanyRepository]
          company <- repo.create(rtjvm)
          gotten  <- repo.getBySlug(company.slug)
        yield (company, gotten)

        program.assert():
          case (c, Some(g)) => c == g
          case _ => false,

      test("getAll - empty"):
        val program = for
          repo      <- ZIO.service[CompanyRepository]
          companies <- repo.getAll
        yield companies

        program.assert()(_.isEmpty),

      test("getAll - full"):
        val program = for
          repo   <- ZIO.service[CompanyRepository]
          comp1  <- repo.create(rtjvm)
          comp2  <- repo.create(Company(-1L, "clock-the-jvm", "Clock the JVM", "clockthejvm.com"))
          gotten <- repo.getAll
        yield (comp1, comp2, gotten)

        program.assert():
          case (c1, c2, gotten) => gotten.toSet == Set(c1, c2),

      test("update record - bad id"):
        val program = for
          repo <- ZIO.service[CompanyRepository]
          err  <- repo.update(10L, _.copy(slug = "boo")).flip
        yield err

        program.assert()(_.getMessage.contains("Could not update missing id")),

      test("update record - valid change"):
        val program = for
          repo    <- ZIO.service[CompanyRepository]
          company <- repo.create(rtjvm)
          updated <- repo.update(company.id, _.copy(url = "blog.rockthejvm.com"))
          fetched <- repo.getById(company.id)
        yield (updated, fetched)

        program.assert():
          case (u, Some(f)) => u == f
          case _ => false,

      test("delete record - bad id"):
        val program = for
          repo <- ZIO.service[CompanyRepository]
          err  <- repo.delete(99L).flip
        yield err

        program.assert()(_.getMessage.contains("Could not delete missing id")),

      test("delete record - valid id"):
        val program = for
          repo    <- ZIO.service[CompanyRepository]
          company <- repo.create(Company(-1L, "rock-the-jvm", "Rock the JVM", "rockthejvm.com"))
          _       <- repo.delete(company.id)
          fetched <- repo.getById(company.id)
        yield fetched

        program.assert()(_.isEmpty),

    ).provide(
      CompanyRepositoryLive.layer,
      dataSourceLayer,
      Repository.quillLayer,
      Scope.default
    )
  end spec

  private val rtjvm = Company(-1L, "rock-the-jvm", "Rock the JVM", "rockthejvm.com")

  def createContainer(): Task[PostgreSQLContainer[Nothing]] =
    ZIO.attempt:
      val container: PostgreSQLContainer[Nothing] =
        PostgreSQLContainer("postgres").withInitScript("sql/companies.sql")
      container.start()
      container
      
  def closeContainer(container: PostgreSQLContainer[Nothing]): UIO[Unit] =
    ZIO.attempt(container.stop()).ignoreLogged

  def createDataSource(container: PostgreSQLContainer[Nothing]): Task[DataSource] =
    ZIO.attempt:
      val dataSource = new PGSimpleDataSource()
      dataSource.setUrl(container.getJdbcUrl)
      dataSource.setUser(container.getUsername)
      dataSource.setPassword(container.getPassword)
      dataSource

  val dataSourceLayer: ZLayer[Any with Scope, Throwable, DataSource] = ZLayer:
    for
      container  <- ZIO.acquireRelease(createContainer())(closeContainer)
      dataSource <- createDataSource(container)
    yield dataSource
end CompanyRepositorySpec
