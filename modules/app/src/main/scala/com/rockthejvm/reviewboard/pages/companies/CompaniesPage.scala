package com.rockthejvm.reviewboard.pages.companies

import com.raquo.laminar.api.L.*
import com.raquo.laminar.nodes.ReactiveHtmlElement
import com.rockthejvm.reviewboard.domain.data.Company
import org.scalajs.dom
import org.scalajs.dom.HTMLElement

object CompaniesPage:
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
            div("TODO filter panel here")
          ),
          div(
            cls := "col-lg-8",
            CompanyCard(dummyCompany),
            CompanyCard(dummyCompany)
          )
        )
      )
    )

  private val dummyCompany = Company(
    1L,
    "dummy-company",
    "Dummy Company Simple",
    "https://dummycompany.com"
  )
