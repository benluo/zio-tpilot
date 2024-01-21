package com.rockthejvm.reviewboard.pages

import com.raquo.laminar.api.L.*
import com.raquo.laminar.nodes.ReactiveHtmlElement
import com.rockthejvm.reviewboard.common.Constants
import org.scalajs.dom
import org.scalajs.dom.HTMLElement

/** State of a user-filled form */
trait FormState:
  /** A list of options of errors that can occur */
  def errorList: List[Option[String]]

  /** Whether to display success/error status */
  def showStatus: Boolean

  /** Optional success message */
  def maybeSuccess: Option[String]

  /** The first defined error in errorList */
  def maybeError: Option[String] =
    errorList.find(_.isDefined).flatten

  /** If there is at least one error */
  def hasErrors: Boolean =
    errorList.exists(_.isDefined)

  /** An optional status message to display */
  def maybeStatus: Option[Either[String, String]] =
    maybeError
      .map(Left(_))
      .orElse(maybeSuccess.map(Right(_)))
      .filter(_ => showStatus)
end FormState

/** A page containing a user-filled form
  * @param title
  *   the title of the page to display above the form
  * @tparam S
  *   implementation of FormState to use
  */
abstract class FormPage[S <: FormState](title: String):
  /** The current (reactive) form state */
  final val stateVar: Var[S] = Var(basicState)
  def basicState: S

  /** The children/form input elements to render */
  def renderChildren(): List[ReactiveHtmlElement[HTMLElement]]

  final def apply(): ReactiveHtmlElement[HTMLElement] =
    div(
      onUnmountCallback(_ => stateVar.set(basicState)),
      cls := "row",
      div(
        cls := "col-md-5 p-0",
        div(
          cls := "logo",
          img(
            cls := "home-logo",
            src := Constants.logoImage,
            alt := "Rock the JVM"
          )
        )
      ),
      div(
        cls := "col-md-7",
        div(
          cls := "form-section",
          div(cls := "top-section", h1(span(title))),
          children <-- stateVar.signal
            .map(_.maybeStatus)
            .map(renderStatus)
            .map(_.toList),
          form(
            nameAttr := "signin",
            cls      := "form",
            idAttr   := "form",
            renderChildren()
          )
        )
      )
    )

  private def renderStatus(status: Option[Either[String, String]]) =
    status.map:
      case Left(err) =>
        div(cls := "page-status-errors", err)
      case Right(msg) =>
        div(cls := "page-status-success", msg)

  /** Render a form input element
    * @param name
    *   the name to display above the input
    * @param uid
    *   the input id
    * @param kind
    *   the input type
    * @param isRequired
    *   whether the input is required to submit the form
    * @param plcHolder
    *   the placeholder to text to display
    * @param updateFn
    *   how to update form state when input's value changes
    * @return
    *   The reactive input element
    */
  def renderInput(
      name: String,
      uid: String,
      kind: String,
      isRequired: Boolean,
      plcHolder: String,
      updateFn: (S, String) => S
  ): ReactiveHtmlElement[HTMLElement] =
    div(
      cls := "row",
      div(
        cls := "col-md-12",
        div(
          cls := "form-input",
          label(
            forId := uid,
            cls   := "form-label",
            if isRequired then span("*") else span(),
            name
          ),
          input(
            tpe         := kind,
            cls         := "form-control",
            idAttr      := uid,
            placeholder := plcHolder,
            onInput.mapToValue --> stateVar.updater(updateFn)
          )
        )
      )
    )
