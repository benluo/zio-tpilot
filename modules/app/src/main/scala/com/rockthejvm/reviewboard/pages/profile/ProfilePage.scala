package com.rockthejvm.reviewboard.pages.profile

import com.raquo.laminar.api.L.*
import com.raquo.laminar.nodes.ReactiveHtmlElement
import com.rockthejvm.reviewboard.core.Session
import com.rockthejvm.reviewboard.pages.{FormPage, FormState}
import com.rockthejvm.reviewboard.core.ZJS.*
import com.rockthejvm.reviewboard.http.requests.UpdatePasswordRequest
import org.scalajs.dom.HTMLElement
import zio.*

case class ChangePasswordState(
    password: String = "",
    newPassword: String = "",
    confirmPassword: String = "",
    upstreamStatus: Option[Either[String, String]] = None,
    override val showStatus: Boolean = false
) extends FormState:
  override val errorList: List[Option[String]] =
    List(
      Option.when(password.isEmpty)("Password cannot be empty"),
      Option.when(newPassword.isEmpty)("New password cannot be empty"),
      Option.when(confirmPassword != newPassword)("Passwords must match")
    ) ++ upstreamStatus.map(_.left.toOption).toList

  override val maybeSuccess: Option[String] =
    upstreamStatus.flatMap(_.toOption)
end ChangePasswordState

object ProfilePage extends FormPage[ChangePasswordState]("Profile"):
  override val stateVar: Var[ChangePasswordState] = Var(ChangePasswordState())

  private def submitter(email: String) = Observer[ChangePasswordState]: state =>
    if state.hasErrors then stateVar.update(_.copy(showStatus = true))
    else
      useBackend(
        _.user.updatePasswordEndpoint(
          UpdatePasswordRequest(email, state.password, state.newPassword)
        )
      )
        .map: _ =>
          stateVar.update(
            _.copy(
              showStatus = true,
              upstreamStatus = Some(Right("Password successfully changed."))
            )
          )
        .tapError: e =>
          ZIO.succeed:
            stateVar.update(
              _.copy(
                showStatus = true,
                upstreamStatus = Some(Left(e.getMessage))
              )
            )
        .runJs

  override def renderChildren(): List[ReactiveHtmlElement[HTMLElement]] =
    Session.getUserState
      .map(_.email)
      .map: email =>
        List(
          renderInput(
            "Password",
            "password-input",
            "password",
            true,
            "Your password",
            (s, p) =>
              s.copy(password = p, showStatus = false, upstreamStatus = None)
          ),
          renderInput(
            "New Password",
            "new-password-input",
            "password",
            true,
            "New password",
            (s, p) =>
              s.copy(newPassword = p, showStatus = false, upstreamStatus = None)
          ),
          renderInput(
            "Confirm Password",
            "confirm-password-input",
            "password",
            true,
            "Confirm password",
            (s, p) =>
              s.copy(
                confirmPassword = p,
                showStatus = false,
                upstreamStatus = None
              )
          ),
          button(
            tpe := "button",
            "Change Password",
            onClick.preventDefault.mapTo(stateVar.now()) --> submitter(email)
          )
        )
      .getOrElse(
        List(
          div(
            cls := "centered-text",
            "Ouch! it seems you're not logged in yet."
          )
        )
      )
