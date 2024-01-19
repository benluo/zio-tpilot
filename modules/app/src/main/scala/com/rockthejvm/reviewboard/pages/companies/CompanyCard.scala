package com.rockthejvm.reviewboard.pages.companies

import com.rockthejvm.reviewboard.common.*
import com.rockthejvm.reviewboard.components.Anchors.navLink
import com.rockthejvm.reviewboard.domain.data.Company
import com.raquo.laminar.api.L.*
import com.raquo.laminar.nodes.ReactiveHtmlElement
import org.scalajs.dom.HTMLElement

object CompanyCard:
  def apply(company: Company): ReactiveHtmlElement[HTMLElement] =
    div(
      cls := "jvm-recent-companies-cards",
      div(
        cls := "jvm-recent-companies-card-img",
        picture(company)
      ),
      div(
        cls := "jvm-recent-companies-card-contents",
        h5(
          navLink(company.name, s"/company/${company.id}", "company-title-link")
        ),
        summary(company)
      ),
      action(company)
    )

  private def picture(company: Company) =
    img(
      cls := "img-fluid",
      src := company.image.getOrElse(Constants.companyLogoPlaceholder),
      alt := company.name
    )

  private def summary(company: Company) =
    div(
      cls := "company-summary",
      detail("location-dot", fullLocationString(company)),
      detail("tags", company.tags.mkString(", "))
    )

  private def detail(icon: String, value: String) =
    div(
      cls := "company-detail",
      i(cls := s"fa fa-$icon company-detail-icon"),
      p(cls := "company-detail-value", value)
    )

  private def fullLocationString(company: Company) =
    (company.location, company.country) match
      case (Some(l), Some(c)) => s"$l, $c"
      case (Some(l), None)    => l
      case (None, Some(c))    => c
      case (None, None)       => "Anywhere"

  private def action(company: Company) =
    div(
      cls := "jvm-recent-companies-card-btn-apply",
      a(
        href   := company.url,
        target := "blank",
        button(
          tpe := "button",
          cls := "btn btn-danger rock-action-btn",
          "View"
        )
      )
    )
