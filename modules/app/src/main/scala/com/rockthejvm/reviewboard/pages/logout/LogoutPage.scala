package com.rockthejvm.reviewboard.pages.logout

import com.raquo.laminar.api.L.*
import com.raquo.laminar.nodes.ReactiveHtmlElement
import com.rockthejvm.reviewboard.core.Session
import com.rockthejvm.reviewboard.pages.{FormPage, FormState}
import org.scalajs.dom.HTMLElement

case class LogoutPageState() extends FormState:
  override def errorList: List[Option[String]] = Nil
  override def showStatus: Boolean = false
  override def maybeSuccess: Option[String] = None

object LogoutPage extends FormPage[LogoutPageState]("Log Out"):
  override def basicState: LogoutPageState = LogoutPageState()
  override def renderChildren(): List[ReactiveHtmlElement[HTMLElement]] =
    List(
      div(
        onMountCallback(_ => Session.clearUserState()),
        cls := "centered-text",
        "You have been successfully logged out."
      )
    )
