package com.rockthejvm.reviewboard.components

import com.rockthejvm.reviewboard.pages.*
import com.raquo.laminar.api.L.*
import com.raquo.laminar.nodes.ReactiveHtmlElement
import com.rockthejvm.reviewboard.pages.companies.CompaniesPage
import com.rockthejvm.reviewboard.pages.forgot.ForgotPage
import com.rockthejvm.reviewboard.pages.login.LoginPage
import com.rockthejvm.reviewboard.pages.signup.SignUpPage
import com.rockthejvm.reviewboard.pages.logout.LogoutPage
import com.rockthejvm.reviewboard.pages.profile.ProfilePage
import com.rockthejvm.reviewboard.pages.recover.RecoverPage
import frontroute.*
import org.scalajs.dom.HTMLElement

object Router:
  def apply(): ReactiveHtmlElement[HTMLElement] =
    mainTag(
      routes(
        div(
          cls := "container-fluid",
          (pathEnd | path("companies"))(CompaniesPage()),
          path("login")(LoginPage()),
          path("signup")(SignUpPage()),
          path("profile")(ProfilePage()),
          path("logout")(LogoutPage()),
          path("forgot")(ForgotPage()),
          path("recover")(RecoverPage()),
          noneMatched(NotFoundPage())
        )
      )
    )
