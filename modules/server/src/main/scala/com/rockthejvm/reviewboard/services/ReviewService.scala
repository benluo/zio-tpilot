package com.rockthejvm.reviewboard.services

import com.rockthejvm.reviewboard.http.requests.CreateReviewRequest
import com.rockthejvm.reviewboard.domain.data.Review
import com.rockthejvm.reviewboard.repositories.ReviewRepository
import zio.*

trait ReviewService:
  def create(req: CreateReviewRequest): Task[Review]
  def getAll: Task[List[Review]]
  def getById(id: Long): Task[Option[Review]]
  def getByCompanyId(id: Long): Task[List[Review]]
  def getByUserId(id: Long): Task[List[Review]]
  
class ReviewServiceLive private (repo: ReviewRepository) extends ReviewService:
  override def create(req: CreateReviewRequest): Task[Review] =
    repo.create(req.toReview(-1L))

  override def getAll: Task[List[Review]] =
    repo.getAll

  override def getById(id: Long): Task[Option[Review]] =
    repo.getById(id)

  override def getByCompanyId(id: Long): Task[List[Review]] =
    repo.getByCompanyId(id)

  override def getByUserId(id: Long): Task[List[Review]] =
    // TODO: implement when user endpoints are implemented
    ???

object ReviewServiceLive:
  val layer: ZLayer[ReviewRepository, Nothing, ReviewServiceLive] =
    ZLayer:
      ZIO.service[ReviewRepository].map(new ReviewServiceLive(_))
