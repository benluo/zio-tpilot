package com.rockthejvm.reviewboard.pages

import com.raquo.laminar.api.L.*
import com.raquo.laminar.nodes.ReactiveHtmlElement
import org.scalajs.dom
import org.scalajs.dom.HTMLElement

object CompaniesPage:
  def apply(): ReactiveHtmlElement[HTMLElement] =
    div("companies page")
