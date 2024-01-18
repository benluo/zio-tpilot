package com.rockthejvm.reviewboard

import com.raquo.laminar.api.L.*
import com.rockthejvm.reviewboard.components.{Header, Router}
import com.rockthejvm.reviewboard.core.Session
import org.scalajs.dom
import frontroute.*

object App:
  private val app =
    div(
      onMountCallback(_ => Session.loadUserState()),
      Header(),
      Router()
    ).amend(LinkHandler.bind)

  def main(args: Array[String]): Unit =
    val containerNode = dom.document.querySelector("#app")
    render(containerNode, app)
