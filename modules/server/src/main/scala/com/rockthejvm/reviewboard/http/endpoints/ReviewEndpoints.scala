package com.rockthejvm.reviewboard.http.endpoints

import com.rockthejvm.reviewboard.domain.data.Review
import com.rockthejvm.reviewboard.http.requests.CreateReviewRequest
import sttp.tapir.*
import sttp.tapir.generic.auto.*
import sttp.tapir.json.zio.jsonBody

trait ReviewEndpoints extends Endpoints:
  val createEndpoint: SecureEP[CreateReviewRequest, Review] =
    secureEndpoint
      .tag("reviews")
      .name("create")
      .description("create a review for a company")
      .in("reviews")
      .post
      .in(jsonBody[CreateReviewRequest])
      .out(jsonBody[Review])

  val getAllEndpoint: EP[Unit, List[Review]] =
    baseEndpoint
      .tag("reviews")
      .name("getAll")
      .description("get all review listings")
      .in("reviews")
      .get
      .out(jsonBody[List[Review]])

  val getByIdEndpoint: EP[Long, Option[Review]] =
    baseEndpoint
      .tag("reviews")
      .name("getById")
      .description("get review by id")
      .in("reviews" / path[Long]("id"))
      .get
      .out(jsonBody[Option[Review]])
    
  val getByCompanyIdEndpoint: EP[Long, List[Review]] =
    baseEndpoint
      .tag("reviews")
      .name("getByCompanyId")
      .description("get reviews for a company by id or slug")
      .in("reviews" / "company" / path[Long]("id"))
      .get
      .out(jsonBody[List[Review]])
    
  val getByUserIdEndpoint: EP[Long, List[Review]] =
    baseEndpoint
      .tag("reviews")
      .name("getByUserId")
      .description("get reviews written by a user")
      .in("reviews" / "user" / path[Long]("id"))
      .get
      .out(jsonBody[List[Review]])
