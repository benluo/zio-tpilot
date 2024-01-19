package com.rockthejvm.reviewboard.services

import com.rockthejvm.reviewboard.domain.data.Review
import com.rockthejvm.reviewboard.http.requests.CreateReviewRequest
import com.rockthejvm.reviewboard.repositories.ReviewRepository
import zio.*
import zio.test.*

import java.time.Instant

object ReviewServiceSpec extends ZIOSpecDefault:
  private val goodReview =
    Review(1L, 1L, 1L, 5, 5, 5, 5, 10, "all good", Instant.now(), Instant.now())

  private val stubRepoLayer =
    ZLayer.succeed:
        new ReviewRepository:
          override def getByCompanyId(id: Long): Task[List[Review]] =
            ZIO.succeed(if id == goodReview.companyId then List(goodReview) else Nil)

          override def getByUserId(id: Long): Task[List[Review]] =
            ZIO.succeed(if id == goodReview.userId then List(goodReview) else Nil)

          override def create(item: Review): Task[Review] =
            ZIO.succeed(goodReview)

          override def update(id: Long, op: Review => Review): Task[Review] =
            if id != goodReview.id then ZIO.fail(new RuntimeException("bad id"))
            else ZIO.succeed(op(goodReview))

          override def delete(id: Long): Task[Review] =
            update(id, identity)

          override def getById(id: Long): Task[Option[Review]] =
            update(id, identity).map(Option.apply).catchAll(_ => ZIO.succeed(None))

          override def getAll: Task[List[Review]] =
            ZIO.succeed(List(goodReview))
  end stubRepoLayer

  override def spec: Spec[TestEnvironment with Scope, Any] =
    suite("ReviewServiceSpec")(
      test("create"):
          for
            service <- ZIO.service[ReviewService]
            review <- service.create(
              CreateReviewRequest(
                companyId = goodReview.companyId,
                management = goodReview.management,
                culture = goodReview.culture,
                salary = goodReview.salary,
                benefits = goodReview.benefits,
                wouldRecommend = goodReview.wouldRecommend,
                review = goodReview.review
              ),
              goodReview.userId
            )
          yield assertTrue(review == goodReview)
      ,
      test("getById"):
          for
            service <- ZIO.service[ReviewService]
            review  <- service.getById(goodReview.id)
          yield assertTrue(review.contains(goodReview))
      ,
      test("getByCompanyId"):
          for
            service <- ZIO.service[ReviewService]
            reviews <- service.getByCompanyId(goodReview.companyId)
          yield assertTrue(reviews.contains(goodReview))
      ,
      test("getByUserId"):
          for
            service <- ZIO.service[ReviewService]
            reviews <- service.getByUserId(goodReview.userId)
          yield assertTrue(reviews.contains(goodReview)),
    ).provide(
      stubRepoLayer,
      ReviewServiceLive.layer
    )
