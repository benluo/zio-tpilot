package com.rockthejvm.reviewboard.http.endpoints

import com.rockthejvm.reviewboard.domain.data.Review
import com.rockthejvm.reviewboard.http.requests.CreateReviewRequest
import sttp.tapir.*
import sttp.tapir.generic.auto.*
import sttp.tapir.json.zio.jsonBody

trait ReviewEndpoints:
  private type EP[I, O] = Endpoint[Unit, I, Unit, O, Any]

  val createEndpoint: EP[CreateReviewRequest, Review] =
    endpoint
      .tag("reviews")
      .name("create")
      .description("create a review for a company")
      .in("reviews")
      .post
      .in(jsonBody[CreateReviewRequest])
      .out(jsonBody[Review])

  val getAllEndpoint: EP[Unit, List[Review]] =
    endpoint
      .tag("reviews")
      .name("getAll")
      .description("get all review listings")
      .in("reviews")
      .get
      .out(jsonBody[List[Review]])

  val getByIdEndpoint: EP[String, Option[Review]] =
    endpoint
      .tag("reviews")
      .name("getById")
      .description("get review by id")
      .in("reviews" / path[String]("id"))
      .get
      .out(jsonBody[Option[Review]])
    
  val getByCompanyIdEndpoint: EP[String, List[Review]] =
    endpoint
      .tag("reviews")
      .name("getByCompanyId")
      .description("get reviews for a company by id or slug")
      .in("reviews" / "company" / path[String]("id"))
      .get
      .out(jsonBody[List[Review]])
    
  val getByUserIdEndpoint: EP[String, List[Review]] =
    endpoint
      .tag("reviews")
      .name("getByUserId")
      .description("get reviews written by a user")
      .in("reviews" / "user" / path[String]("id"))
      .get
      .out(jsonBody[List[Review]])
