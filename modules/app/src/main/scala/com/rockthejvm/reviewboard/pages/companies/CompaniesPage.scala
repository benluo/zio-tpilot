package com.rockthejvm.reviewboard.pages.companies

import com.raquo.laminar.api.L.*
import com.raquo.laminar.nodes.ReactiveHtmlElement
import com.rockthejvm.reviewboard.domain.data.Company
import com.rockthejvm.reviewboard.core.ZJS.*
import org.scalajs.dom
import org.scalajs.dom.HTMLElement

object CompaniesPage:
  private val filterPanel = new FilterPanel
  private val companyEvents =
    useBackend(_.company.getAllEndpoint(()))
      .toEventStream
      .mergeWith:
        filterPanel.triggerFilters.flatMap: newFilter =>
          useBackend(_.company.searchEndpoint(newFilter)).toEventStream

  def apply(): ReactiveHtmlElement[HTMLElement] =
    sectionTag(
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
            filterPanel()
          ),
          div(
            cls := "col-lg-8",
            children <-- companyEvents.map(_.map(CompanyCard.apply))
          )
        )
      )
    )
