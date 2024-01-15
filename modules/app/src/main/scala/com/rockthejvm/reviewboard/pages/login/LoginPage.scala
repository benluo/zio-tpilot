package com.rockthejvm.reviewboard.pages.login

import com.raquo.laminar.api.L.*
import com.raquo.laminar.nodes.ReactiveHtmlElement
import org.scalajs.dom.HTMLElement

object LoginPage:
  def apply(): ReactiveHtmlElement[HTMLElement] =
    div("log in page")