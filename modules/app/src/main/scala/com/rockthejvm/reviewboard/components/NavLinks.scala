package com.rockthejvm.reviewboard.components

import com.raquo.laminar.api.L.*
import com.raquo.laminar.nodes.ReactiveHtmlElement
import com.rockthejvm.reviewboard.core.Session
import com.rockthejvm.reviewboard.domain.data.UserToken
import org.scalajs.dom.HTMLElement

object NavLinks:
  def apply(): ReactiveHtmlElement[HTMLElement] =
    ul(
      cls := "navbar-nav ms-auto menu align-center expanded text-center SMN_effect-3",
      children <-- Session.userState.signal.map: maybeToken =>
        links(maybeToken).map:
          case (text, location) =>
            li(
              cls := "nav-item",
              Anchors.navLink(text, location, "jvm-item")
            )
    )

  private val constantLinks = List("Companies" -> "/companies")

  private val authedLinks =
    List(
      "Add Company" -> "/post",
      "Profile"     -> "/profile",
      "Log Out"     -> "/logout"
    )

  private val unAuthedLinks =
    List("Log In" -> "/login", "Sign Up" -> "/signup")

  private def links(maybeToken: Option[UserToken]) =
    constantLinks ++ maybeToken.fold(unAuthedLinks)(_ => authedLinks)
