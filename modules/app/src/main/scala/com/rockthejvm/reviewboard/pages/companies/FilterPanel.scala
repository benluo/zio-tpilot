package com.rockthejvm.reviewboard.pages.companies

import com.raquo.laminar.api.L.*
import com.raquo.laminar.codecs.StringAsIsCodec
import com.raquo.laminar.nodes.ReactiveHtmlElement
import com.rockthejvm.reviewboard.core.ZJS.*
import com.rockthejvm.reviewboard.domain.data.CompanyFilter
import org.scalajs.dom.HTMLElement

class FilterPanel:
  private val possibleFilter = Var[CompanyFilter](CompanyFilter.empty)
  private def fetchFilter(): Unit =
    useBackend(_.company.allFiltersEndpoint(()))
      .map(possibleFilter.set)
      .runJs()

  private case class CheckValueEvent(
      groupName: String,
      value: String,
      checked: Boolean
  )
  private val checkEvents = EventBus[CheckValueEvent]()

  private val applyFiltersClicks = EventBus[Unit]()
  private val dirty =
    applyFiltersClicks.events
      .mapTo(false)
      .mergeWith(checkEvents.events.mapTo(true))

  private val state: Signal[CompanyFilter] =
    checkEvents.events
      .scanLeft(Map[String, Set[String]]()): (acc, event) =>
        event match
          case CheckValueEvent(groupName, value, checked) =>
            if checked then
              acc + (groupName    -> (acc.getOrElse(groupName, Set()) + value))
            else acc + (groupName -> (acc.getOrElse(groupName, Set()) - value))
      .map: checkMap =>
        CompanyFilter(
          locations = checkMap.getOrElse(GROUP_LOCATIONS, Set()).toList,
          countries = checkMap.getOrElse(GROUP_COUNTRIES, Set()).toList,
          industries = checkMap.getOrElse(GROUP_INDUSTRIES, Set()).toList,
          tags = checkMap.getOrElse(GROUP_TAGS, Set()).toList
        )

  private val GROUP_LOCATIONS  = "Locations"
  private val GROUP_COUNTRIES  = "Countries"
  private val GROUP_INDUSTRIES = "Industries"
  private val GROUP_TAGS       = "Tags"

  val triggerFilters: EventStream[CompanyFilter] =
    applyFiltersClicks.events.withCurrentValueOf(state)

  def apply(): ReactiveHtmlElement[HTMLElement] =
    div(
      onMountCallback(_ => fetchFilter()),
      cls    := "accordion accordion-flush",
      idAttr := "accordionFlushExample",
      div(
        cls := "accordion-item",
        h2(
          cls    := "accordion-header",
          idAttr := "flush-headingOne",
          button(
            cls    := "accordion-button",
            idAttr := "accordion-search-filter",
            `type` := "button",
            htmlAttr("data-bs-toggle", StringAsIsCodec) := "collapse",
            htmlAttr("data-bs-target", StringAsIsCodec) := "#flush-collapseOne",
            htmlAttr("aria-expanded", StringAsIsCodec)  := "true",
            htmlAttr("aria-controls", StringAsIsCodec)  := "flush-collapseOne",
            div(
              cls := "jvm-recent-companies-accordion-body-heading",
              h3(
                span("Search"),
                " Filters"
              )
            )
          )
        ),
        div(
          cls    := "accordion-collapse collapse show",
          idAttr := "flush-collapseOne",
          htmlAttr("aria-labelledby", StringAsIsCodec) := "flush-headingOne",
          htmlAttr(
            "data-bs-parent",
            StringAsIsCodec
          ) := "#accordionFlushExample",
          div(
            cls := "accordion-body p-0",
            filterOptions(GROUP_LOCATIONS, _.locations),
            filterOptions(GROUP_COUNTRIES, _.countries),
            filterOptions(GROUP_INDUSTRIES, _.industries),
            filterOptions(GROUP_TAGS, _.tags),
            applyFiltersButton()
          )
        )
      )
    )

  private def filterOptions(
      groupName: String,
      optsFn: CompanyFilter => List[String]
  ) =
    div(
      cls := "accordion-item",
      h2(
        cls    := "accordion-header",
        idAttr := s"heading$groupName",
        button(
          cls    := "accordion-button collapsed",
          `type` := "button",
          htmlAttr("data-bs-toggle", StringAsIsCodec) := "collapse",
          htmlAttr("data-bs-target", StringAsIsCodec) := s"#collapse$groupName",
          htmlAttr("aria-expanded", StringAsIsCodec)  := "false",
          htmlAttr("aria-controls", StringAsIsCodec)  := s"collapse$groupName",
          groupName
        )
      ),
      div(
        cls    := "accordion-collapse collapse",
        idAttr := s"collapse$groupName",
        htmlAttr("aria-labelledby", StringAsIsCodec) := "headingOne",
        htmlAttr("data-bs-parent", StringAsIsCodec)  := "#accordionExample",
        div(
          cls := "accordion-body",
          div(
            cls := "mb-3",
            children <-- possibleFilter.signal.map(
              optsFn(_).map(checkbox(groupName, _))
            )
          )
        )
      )
    )

  private def checkbox(groupName: String, value: String) =
    div(
      cls := "form-check",
      label(
        cls   := "form-check-label",
        forId := s"filter-$groupName-$value",
        value
      ),
      input(
        cls    := "form-check-input",
        `type` := "checkbox",
        idAttr := s"filter-$groupName-$value",
        onChange.mapToChecked.map(
          CheckValueEvent(groupName, value, _)
        ) --> checkEvents
      )
    )

  private def applyFiltersButton() =
    div(
      cls := "jvm-accordion-search-btn",
      button(
        disabled <-- dirty.toSignal(false).map(v => !v),
        onClick.mapTo(()) --> applyFiltersClicks,
        cls    := "btn btn-primary",
        `type` := "button",
        "Apply Filters"
      )
    )
