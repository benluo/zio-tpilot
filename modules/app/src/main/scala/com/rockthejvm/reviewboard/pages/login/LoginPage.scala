package com.rockthejvm.reviewboard.pages.login

import com.raquo.laminar.api.L.*
import com.raquo.laminar.nodes.ReactiveHtmlElement
import com.rockthejvm.reviewboard.common.Constants
import com.rockthejvm.reviewboard.core.ZJS.*
import com.rockthejvm.reviewboard.http.requests.LoginRequest
import frontroute.BrowserNavigation
import org.scalajs.dom
import org.scalajs.dom.HTMLElement
import zio.*

object LoginPage:
  case class State(
    email: String = "",
    password: String = "",
    upstreamError: Option[String] = None,
    showStatus: Boolean = false
  ):
    private val userEmailError: Option[String] =
      Option.when(!email.matches(Constants.emailRegex))("User email is invalid")
    private val passwordError: Option[String] =
      Option.when(password.isEmpty)("Password can't be empty")
    private val errorList = List(userEmailError, passwordError, upstreamError)
    val maybeError: Option[String] = errorList.find(_.isDefined).flatten.filter(_ => showStatus)
    val hasErrors: Boolean = errorList.exists(_.isDefined)

  private val stateVar = Var(State())

  private val submitter = Observer[State]: state =>
    if state.hasErrors then
      stateVar.update(_.copy(showStatus = true))
    else
      useBackend(_.user.loginEndpoint(LoginRequest(state.email, state.password)))
        .map: userToken =>
          // TODO: set user token
          stateVar.set(State())
          BrowserNavigation.replaceState("/")
        .tapError: e =>
          ZIO.succeed:
            stateVar.update(_.copy(showStatus = true, upstreamError = Some(e.getMessage)))
        .runJs


  def apply(): ReactiveHtmlElement[HTMLElement] =
    div(
      cls := "row",
      div(
        cls := "col-md-5 p-0",
        div(
          cls := "logo",
          img(
            cls := "home-logo",
            src := Constants.logoImage,
            alt := "Rock the JVM"
          )
        )
      ),
      div(
        cls := "col-md-7",
        // right
        div(
          cls := "form-section",
          div(cls := "top-section", h1(span("Log In"))),
          children <-- stateVar.signal
            .map(_.maybeError)
            .map(_.map(renderError))
            .map(_.toList),
          maybeRenderSuccess(),
          form(
            nameAttr := "signin",
            cls := "form",
            idAttr := "form",
            renderInput(
              "Email",
              "email-input",
              "text",
              true,
              "Your email",
              (s, e) => s.copy(email = e, showStatus = false, upstreamError = None)
            ),
            renderInput(
              "Password",
              "password-input",
              "password",
              true,
              "Your password",
              (s, p) => s.copy(password = p, showStatus = false, upstreamError = None)
            ),
            button(
              tpe := "button",
              "Log In",
              onClick.preventDefault.mapTo(stateVar.now()) --> submitter
            )
          )
        )
      )
    )

  private def renderError(error: String) =
    div(cls := "page-status-errors", error)

  private def maybeRenderSuccess(shouldShow: Boolean = false) =
    if shouldShow then
      div(cls := "page-status-success", "This is a success")
    else
      div()

  private def renderInput(
    name: String,
    uid: String,
    kind: String,
    isRequired: Boolean,
    plcHolder: String,
    updateFn: (State, String) => State
  ) =
    div(
      cls := "row",
      div(
        cls := "col-md-12",
        div(
          cls := "form-input",
          label(
            forId := uid,
            cls := "form-label",
            if isRequired then span("*") else span(),
            name
          ),
          input(
            tpe := kind,
            cls := "form-control",
            idAttr := uid,
            placeholder := plcHolder,
            onInput.mapToValue --> stateVar.updater(updateFn)
          )
        )
      )
    )
