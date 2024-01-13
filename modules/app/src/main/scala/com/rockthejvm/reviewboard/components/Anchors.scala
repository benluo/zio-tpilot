package com.rockthejvm.reviewboard.components

import com.raquo.laminar.api.L.*
import com.raquo.laminar.nodes.ReactiveHtmlElement
import org.scalajs.dom.HTMLAnchorElement

object Anchors:
  def NavLink(
    text: String,
    location: String,
    cssClass: String = ""
  ): ReactiveHtmlElement[HTMLAnchorElement] =
    a(
      href := location,
      cls  := cssClass,
      text
    )
