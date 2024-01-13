package com.rockthejvm.reviewboard.components

import com.raquo.laminar.api.L.*
import com.raquo.laminar.nodes.ReactiveHtmlElement
import org.scalajs.dom.HTMLElement

object NavLinks:
  def apply(): ReactiveHtmlElement[HTMLElement] =
    ul(
      cls := "navbar-nav ms-auto menu align-center expanded text-center SMN_effect-3",
      links.map:
        case (text, location) =>
          li(
            cls := "nav-item",
            Anchors.NavLink(text, location, "jvm-item")
          )
    )
    
  private val links =
    List(
      "Companies" -> "/companies",
      "Log In"    -> "/login",
      "Sign Up"   -> "/signup"
    )
