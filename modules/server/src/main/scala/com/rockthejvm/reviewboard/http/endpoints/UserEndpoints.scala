package com.rockthejvm.reviewboard.http.endpoints

import com.rockthejvm.reviewboard.domain.data.UserToken
import com.rockthejvm.reviewboard.http.requests.*
import com.rockthejvm.reviewboard.http.responses.UserResponse
import sttp.tapir.*
import sttp.tapir.json.zio.jsonBody
import sttp.tapir.generic.auto.*

trait UserEndpoints extends Endpoints:
  val createUserEndpoint: EP[RegisterUserRequest, UserResponse] =
    baseEndpoint
      .tag("users")
      .name("login")
      .description("register a new user")
      .in("users")
      .post
      .in(jsonBody[RegisterUserRequest])
      .out(jsonBody[UserResponse])
  
  val updatePasswordEndpoint: SEP[UpdatePasswordRequest, UserResponse] =
    secureEndpoint
      .tag("users")
      .name("updatePassword")
      .description("update user password")
      .in("users" / "password")
      .put
      .in(jsonBody[UpdatePasswordRequest])
      .out(jsonBody[UserResponse])
  
  val deleteEndpoint: SEP[DeleteAccountRequest, UserResponse] =
    secureEndpoint
      .tag("users")
      .name("deleteUser")
      .description("delete a user")
      .in("users")
      .delete
      .in(jsonBody[DeleteAccountRequest])
      .out(jsonBody[UserResponse])

  val loginEndpoint: EP[LoginRequest, UserToken] =
    baseEndpoint
      .tag("users")
      .name("login")
      .description("log in and generate a JWT")
      .in("users" / "login")
      .post
      .in(jsonBody[LoginRequest])
      .out(jsonBody[UserToken])

  val forgotPasswordEndpoint: EP[ForgotPasswordRequest, Unit] =
    baseEndpoint
      .tag("users")
      .name("forgotPassword")
      .description("trigger email for password recovery")
      .in("users" / "forgot")
      .post
      .in(jsonBody[ForgotPasswordRequest])

  val recoverPasswordEndpoint: EP[RecoverPasswordRequest, Unit] =
    baseEndpoint
      .tag("users")
      .name("recoverPassword")
      .description("set new password based on OTP")
      .in("users" / "recover")
      .post
      .in(jsonBody[RecoverPasswordRequest])