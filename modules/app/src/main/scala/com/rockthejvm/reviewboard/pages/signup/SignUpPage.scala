package com.rockthejvm.reviewboard.pages.signup

import com.raquo.laminar.api.L.*
import com.raquo.laminar.nodes.ReactiveHtmlElement
import org.scalajs.dom.HTMLElement

object SignUpPage:
  def apply(): ReactiveHtmlElement[HTMLElement] =
    div("sign up page")
    