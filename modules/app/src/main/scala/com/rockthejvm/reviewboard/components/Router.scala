package com.rockthejvm.reviewboard.components

import com.rockthejvm.reviewboard.pages.*

import com.raquo.laminar.api.L.*
import com.raquo.laminar.nodes.ReactiveHtmlElement
import frontroute.*
import org.scalajs.dom.HTMLElement

object Router:
  def apply(): ReactiveHtmlElement[HTMLElement] =
    mainTag(
      routes(
        div(
          cls := "container-fluid",
          (pathEnd | path("companies")):
            CompaniesPage(),
          path("login"):
            LoginPage(),
          path("signup"):
            SignUpPage(),
          noneMatched:
            NotFoundPage()
        )
      )
    )
