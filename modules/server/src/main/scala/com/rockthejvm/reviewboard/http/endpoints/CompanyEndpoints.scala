package com.rockthejvm.reviewboard.http.endpoints

import com.rockthejvm.reviewboard.domain.data.Company
import com.rockthejvm.reviewboard.http.requests.CreateCompanyRequest
import sttp.tapir.*
import sttp.tapir.generic.auto.*
import sttp.tapir.json.zio.jsonBody

trait CompanyEndpoints extends Endpoints:
  val createEndpoint: SecureEP[CreateCompanyRequest, Company] =
    secureEndpoint
      .tag("companies")
      .name("create")
      .description("create a listing for a company")
      .in("companies")
      .post
      .in(jsonBody[CreateCompanyRequest])
      .out(jsonBody[Company])

  val getAllEndpoint: EP[Unit, List[Company]] =
    baseEndpoint
      .tag("companies")
      .name("getAll")
      .description("get all company listings")
      .in("companies")
      .get
      .out(jsonBody[List[Company]])

  val getByIdEndpoint: EP[String, Option[Company]] =
    baseEndpoint
      .tag("companies")
      .name("getById")
      .description("get company by id or slug")
      .in("companies" / path[String]("id"))
      .get
      .out(jsonBody[Option[Company]])
