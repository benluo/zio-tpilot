package com.rockthejvm.reviewboard.components

import com.rockthejvm.reviewboard.common.*

import com.raquo.laminar.api.L.*
import com.raquo.laminar.codecs.StringAsIsCodec
import com.raquo.laminar.nodes.ReactiveHtmlElement
import org.scalajs.dom
import org.scalajs.dom.HTMLElement

object Header:
  def apply(): ReactiveHtmlElement[HTMLElement] =
    div(
      cls := "container-fluid p-0",
      div(
        cls := "jvm-nav",
        div(
          cls := "container",
          navTag(
            cls := "navbar navbar-expand-lg navbar-light JVM-nav",
            div(
              cls := "container",
              Logo(),
              button(
                cls := "navbar-toggler",
                `type` := "button",
                htmlAttr("data-bs-toggle", StringAsIsCodec) := "collapse",
                htmlAttr("data-bs-target", StringAsIsCodec) := "#navbarNav",
                htmlAttr("aria-controls", StringAsIsCodec) := "navbarNav",
                htmlAttr("aria-expanded", StringAsIsCodec) := "false",
                htmlAttr("aria-label", StringAsIsCodec) := "Toggle navigation",
                span(cls := "navbar-toggler-icon")
              ),
              div(
                cls := "collapse navbar-collapse",
                idAttr := "navbarNav",
                ul(
                  cls := "navbar-nav ms-auto menu align-center expanded text-center SMN_effect-3",
                  div(
                    cls := "collapse navbar-collapse",
                    idAttr := "navbarNav",
                    NavLinks()
                  )
                )
              )
            )
          )
        )
      )
    )

  private def Logo() =
    a(
      href := "/",
      cls := "navbar-brand",
      img(
        cls := "home-logo",
        src := Constants.logoImage,
        alt := "Rock the JVM"
      )
    )
