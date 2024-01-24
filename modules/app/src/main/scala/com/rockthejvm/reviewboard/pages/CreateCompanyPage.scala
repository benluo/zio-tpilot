package com.rockthejvm.reviewboard.pages
import com.raquo.laminar.nodes.ReactiveHtmlElement
import com.rockthejvm.reviewboard.common.Constants
import org.scalajs.dom.HTMLElement

case class CreateCompanyFormState(
    name: String = "",
    url: String = "",
    location: Option[String] = None,
    country: Option[String] = None,
    industry: Option[String] = None,
    image: Option[String] = None,
    tags: List[String] = Nil,
    upstreamStatus: Option[Either[String, String]] = None,
    override val showStatus: Boolean = false
) extends FormState:
  override def errorList: List[Option[String]] =
    List(
      Option.when(name.isEmpty)("Name cannot be empty."),
      Option.when(!url.matches(Constants.urlRegex))("Url must be a valid url.")
    )
  override def maybeSuccess: Option[String] =
    upstreamStatus.flatMap(_.toOption)

object CreateCompanyPage
    extends FormPage[CreateCompanyFormState]("Create Company"):
  override def basicState: CreateCompanyFormState = CreateCompanyFormState()

  override def renderChildren(): List[ReactiveHtmlElement[HTMLElement]] = List()
