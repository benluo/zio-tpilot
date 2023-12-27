package com.rockthejvm.reviewboard.http.requests

import com.rockthejvm.reviewboard.domain.data.Review
import zio.json.{DeriveJsonCodec, JsonCodec}

import java.time.Instant

case class CreateReviewRequest(
  companyId: Long,
  userId: Long,
  management: Int,
  culture: Int,
  salary: Int,
  benefits: Int,
  wouldRecommend: Int,
  review: String
):
  def toReview(id: Long, created: Instant = Instant.now()): Review =
    Review(
      id,
      companyId,
      userId,
      management,
      culture,
      salary,
      benefits,
      wouldRecommend,
      review,
      created,
      created
    )

object CreateReviewRequest:
  given codec: JsonCodec[CreateReviewRequest] =
    DeriveJsonCodec.gen[CreateReviewRequest]
  