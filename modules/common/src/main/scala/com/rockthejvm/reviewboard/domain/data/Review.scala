package com.rockthejvm.reviewboard.domain.data

import java.time.Instant
import zio.json.JsonCodec

/** a review for a company */
final case class Review(
    id: Long,
    companyId: Long,
    userId: Long,
    management: Int,
    culture: Int,
    salary: Int,
    benefits: Int,
    wouldRecommend: Int,
    review: String,
    created: Instant,
    updated: Instant
) derives JsonCodec
