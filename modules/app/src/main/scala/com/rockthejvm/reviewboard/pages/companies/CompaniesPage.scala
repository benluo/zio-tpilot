package com.rockthejvm.reviewboard.pages.companies

import com.raquo.laminar.api.L.*
import com.raquo.laminar.nodes.ReactiveHtmlElement
import com.rockthejvm.reviewboard.domain.data.Company
import com.rockthejvm.reviewboard.core.ZJS.*
import org.scalajs.dom
import org.scalajs.dom.HTMLElement

object CompaniesPage:
  private val companiesBus = EventBus[List[Company]]()

  private def performBackendCall(): Unit =
    useBackend(_.company.getAllEndpoint(()))
      .emitTo(companiesBus)

  def apply(): ReactiveHtmlElement[HTMLElement] =
    sectionTag(
      onMountCallback(_ => performBackendCall()),
      cls := "section-1",
      div(
        cls := "container company-list-hero",
        h1(
          cls := "company-list-title",
          "Rock the JVM Companies Board"
        )
      ),
      div(
        cls := "container",
        div(
          cls := "row jvm-recent-companies-body",
          div(
            cls := "col-lg-4",
            FilterPanel()
          ),
          div(
            cls := "col-lg-8",
            children <-- companiesBus.events.map(_.map(CompanyCard.apply))
          )
        )
      )
    )
