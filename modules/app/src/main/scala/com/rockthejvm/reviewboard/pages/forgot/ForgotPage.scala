package com.rockthejvm.reviewboard.pages.forgot

import com.raquo.laminar.api.L.*
import com.raquo.laminar.nodes.ReactiveHtmlElement
import com.rockthejvm.reviewboard.common.Constants
import com.rockthejvm.reviewboard.components.Anchors
import com.rockthejvm.reviewboard.core.ZJS.*
import com.rockthejvm.reviewboard.http.requests.{ForgotPasswordRequest, RecoverPasswordRequest}
import com.rockthejvm.reviewboard.pages.signup.SignUpPage.renderInput
import com.rockthejvm.reviewboard.pages.{FormPage, FormState}
import org.scalajs.dom.*
import zio.*

case class ForgotState(
    email: String = "",
    upstreamStatus: Option[Either[String, String]] = None,
    override val showStatus: Boolean = false
) extends FormState:
  override val errorList: List[Option[String]] =
    List(
      Option.when(!email.matches(Constants.emailRegex))("Email is invalid")
    ) ++ upstreamStatus.map(_.left.toOption).toList

  override def maybeSuccess: Option[String] =
    upstreamStatus.flatMap(_.toOption)
end ForgotState

object ForgotPage extends FormPage[ForgotState]("Forgot Password"):
  override def basicState: ForgotState = ForgotState()

  private val submitter = Observer[ForgotState]: state =>
    if state.hasErrors then stateVar.update(_.copy(showStatus = true))
    else
      useBackend(
        _.user.forgotPasswordEndpoint(ForgotPasswordRequest(state.email))
      )
        .map: _ =>
          stateVar
            .update(
              _.copy(
                showStatus = true,
                upstreamStatus = Some(Right("Recovery email sent!"))
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
      button(
        tpe := "button",
        "Recover Password",
        onClick.preventDefault.mapTo(stateVar.now()) --> submitter
      ),
      Anchors.navLink(
        "Have a password recovery token?",
        "/recover",
        "auth-link"
      )
    )
end ForgotPage
