package com.rockthejvm.reviewboard.repositories

import com.rockthejvm.reviewboard.domain.data.Review

import zio.*
import zio.test.*

import java.time.Instant

object ReviewRepositorySpec extends ZIOSpecDefault with RepositorySpec:
  override val initScript: String = "sql/reviews.sql"

  private val goodReview =
    Review(1L, 1L, 1L, 5, 5, 5, 5, 10, "all good", Instant.now(), Instant.now())
  private val badReview =
    Review(2L, 1L, 1L, 1, 1, 1, 1, 1, "all bad", Instant.now(), Instant.now())

  override def spec: Spec[TestEnvironment with Scope, Any] =
    suite("ReviewRepositorySpec")(
      test("create review"):
        for
          repo   <- ZIO.service[ReviewRepository]
          review <- repo.create(goodReview)
        yield assertTrue:
          review.management == goodReview.management &&
            review.culture == goodReview.culture &&
            review.salary == goodReview.salary &&
            review.benefits == goodReview.benefits &&
            review.wouldRecommend == goodReview.wouldRecommend &&
            review.review == goodReview.review
      ,
      test("get review(s) by ids - bad id"):
        for
          repo     <- ZIO.service[ReviewRepository]
          byId     <- repo.getById(99L)
          byCompId <- repo.getByCompanyId(99L)
          byUserId <- repo.getByUserId(99L)
        yield assertTrue:
          (byId, byCompId, byUserId) == (None, Nil, Nil)
      ,
      test("get review(s) by ids - good id"):
        for
          repo     <- ZIO.service[ReviewRepository]
          good     <- repo.create(goodReview)
          bad      <- repo.create(badReview)
          byId     <- repo.getById(good.id)
          byCompId <- repo.getByCompanyId(good.companyId)
          byUserId <- repo.getByUserId(good.userId)
        yield assertTrue:
          byId.contains(good) &&
            byCompId.contains(good) &&
            byCompId.contains(bad) &&
            byUserId.contains(good) &&
            byUserId.contains(bad)
      ,
      test("get all"):
        for
          repo <- ZIO.service[ReviewRepository]
          good <- repo.create(goodReview)
          bad  <- repo.create(badReview)
          all  <- repo.getAll
        yield assertTrue:
          all.length == 2 &&
            all.contains(good) &&
            all.contains(bad)
      ,
      test("update"):
        for
          repo     <- ZIO.service[ReviewRepository]
          original <- repo.create(goodReview)
          updated  <- repo.update(original.id, _.copy(review = "SOOO GOOD"))
        yield assertTrue:
          updated.id == original.id &&
            updated.companyId == original.companyId &&
            updated.userId == original.userId &&
            updated.management == original.management &&
            updated.culture == original.culture &&
            updated.salary == original.salary &&
            updated.wouldRecommend == original.wouldRecommend &&
            updated.review == "SOOO GOOD" &&
            updated.created == original.created
      ,
      test("delete"):
        for
          repo    <- ZIO.service[ReviewRepository]
          review  <- repo.create(goodReview)
          _       <- repo.delete(review.id)
          fetched <- repo.getById(review.id)
        yield assertTrue(fetched.isEmpty),
    ).provide(
      ReviewRepositoryLive.layer,
      dataSourceLayer,
      Repository.quillLayer,
      Scope.default
    )
