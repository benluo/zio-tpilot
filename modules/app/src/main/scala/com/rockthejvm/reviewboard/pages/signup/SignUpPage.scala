package com.rockthejvm.reviewboard.pages.signup

import com.raquo.laminar.api.L.*
import com.raquo.laminar.nodes.ReactiveHtmlElement
import com.rockthejvm.reviewboard.common.Constants
import com.rockthejvm.reviewboard.core.ZJS.*
import com.rockthejvm.reviewboard.http.requests.RegisterUserRequest
import com.rockthejvm.reviewboard.pages.{FormPage, FormState}
import org.scalajs.dom.HTMLElement
import zio.ZIO

case class SignUpFormState(
    email: String = "",
    password: String = "",
    confirmPassword: String = "",
    upstreamStatus: Option[Either[String, String]] = None,
    override val showStatus: Boolean = false
) extends FormState:
  private val userEmailError: Option[String] =
    Option.when(!email.matches(Constants.emailRegex))("User email is invalid")

  private val passwordError: Option[String] =
    Option.when(password.isEmpty)("Password can't be empty")

  private val confirmPasswordError: Option[String] =
    Option.when(confirmPassword != password)("Passwords must match")

  override val errorList: List[Option[String]] =
    List(userEmailError, passwordError, confirmPasswordError) ++
      upstreamStatus.map(_.left.toOption).toList

  override val maybeSuccess: Option[String] = upstreamStatus.flatMap(_.toOption)
end SignUpFormState

object SignUpPage extends FormPage[SignUpFormState]("Sign Up"):
  override val stateVar: Var[SignUpFormState] = Var(SignUpFormState())

  private val submitter = Observer[SignUpFormState]: state =>
    if state.hasErrors then stateVar.update(_.copy(showStatus = true))
    else
      useBackend(_.user.createUserEndpoint(RegisterUserRequest(state.email, state.password)))
        .map: _ =>
          stateVar
            .update(_.copy(showStatus = true, upstreamStatus = Some(Right("Account created!"))))
        .tapError: e =>
          ZIO.succeed:
              stateVar
                .update(_.copy(showStatus = true, upstreamStatus = Some(Left(e.getMessage))))
        .runJs

  override def renderChildren(): List[ReactiveHtmlElement[HTMLElement]] =
    List(
      renderInput(
        "Email",
        "email-input",
        "text",
        true,
        "Your email",
        (s, e) => s.copy(email = e, showStatus = false, upstreamStatus = None)
      ),
      renderInput(
        "Password",
        "password-input",
        "password",
        true,
        "Your password",
        (s, p) => s.copy(password = p, showStatus = false, upstreamStatus = None)
      ),
      renderInput(
        "Confirm Password",
        "confirm-password-input",
        "password",
        true,
        "Your password",
        (s, p) => s.copy(confirmPassword = p, showStatus = false, upstreamStatus = None)
      ),
      button(
        tpe := "button",
        "Sign Up",
        onClick.preventDefault.mapTo(stateVar.now()) --> submitter
      )
    )
