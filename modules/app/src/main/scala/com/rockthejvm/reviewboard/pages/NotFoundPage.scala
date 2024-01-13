package com.rockthejvm.reviewboard.pages

import com.raquo.laminar.api.L.*
import com.raquo.laminar.nodes.ReactiveHtmlElement
import org.scalajs.dom.HTMLElement

object NotFoundPage:
  def apply(): ReactiveHtmlElement[HTMLElement] =
    div("(404 Not Found): You lost?")
