package com.rockthejvm.reviewboard.pages.recover

import com.raquo.laminar.api.L.*
import com.raquo.laminar.nodes.ReactiveHtmlElement
import com.rockthejvm.reviewboard.common.Constants
import com.rockthejvm.reviewboard.components.Anchors
import com.rockthejvm.reviewboard.core.ZJS.*
import com.rockthejvm.reviewboard.http.requests.RecoverPasswordRequest
import com.rockthejvm.reviewboard.pages.{FormPage, FormState}
import org.scalajs.dom.*
import zio.*

case class RecoverState(
    email: String = "",
    recoveryToken: String = "",
    newPassword: String = "",
    confirmPassword: String = "",
    upstreamStatus: Option[Either[String, String]] = None,
    override val showStatus: Boolean = false
) extends FormState:
  override def errorList: List[Option[String]] =
    List(
      Option.when(!email.matches(Constants.emailRegex))("Invalid email"),
      Option.when(recoveryToken.isEmpty)("Recovery token cannot be empty"),
      Option.when(newPassword.isEmpty)("Password cannot be empty"),
      Option.when(confirmPassword != newPassword)("Passwords must match")
    ) ++ upstreamStatus.map(_.left.toOption).toList

  override def maybeSuccess: Option[String] =
    upstreamStatus.flatMap(_.toOption)
end RecoverState

object RecoverPage extends FormPage[RecoverState]("Recover"):
  override def basicState: RecoverState = RecoverState()

  private val submitter = Observer[RecoverState]: state =>
    if state.hasErrors then stateVar.update(_.copy(showStatus = true))
    else
      useBackend(
        _.user.recoverPasswordEndpoint(
          RecoverPasswordRequest(
            state.email,
            state.recoveryToken,
            state.newPassword
          )
        )
      )
        .map: _ =>
          stateVar
            .update(
              _.copy(
                showStatus = true,
                upstreamStatus = Some(Right("Success! You can log in now."))
              )
            )
        .tapError: e =>
          ZIO.succeed:
            stateVar
              .update(
                _.copy(
                  showStatus = true,
                  upstreamStatus = Some(Left(e.getMessage))
                )
              )
        .runJs()

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
        "Recovery Token",
        "token-input",
        "text",
        true,
        "Your recovery token",
        (s, t) =>
          s.copy(recoveryToken = t, showStatus = false, upstreamStatus = None)
      ),
      renderInput(
        "New Password",
        "password-input",
        "password",
        true,
        "Your new password",
        (s, p) =>
          s.copy(newPassword = p, showStatus = false, upstreamStatus = None)
      ),
      renderInput(
        "Confirm Password",
        "confirm-password-input",
        "password",
        true,
        "Your password",
        (s, p) =>
          s.copy(confirmPassword = p, showStatus = false, upstreamStatus = None)
      ),
      button(
        tpe := "button",
        "Reset Password",
        onClick.preventDefault.mapTo(stateVar.now()) --> submitter
      ),
      Anchors.navLink("Need a recovery token?", "/forgot", "auth-link")
    )
