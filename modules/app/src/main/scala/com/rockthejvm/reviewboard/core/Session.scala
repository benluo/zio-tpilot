package com.rockthejvm.reviewboard.core

import com.raquo.laminar.api.L.*
import com.rockthejvm.reviewboard.domain.data.UserToken
import scala.scalajs.js.*

object Session:
  private val stateName = "userState"
  val userState         = Var(Option.empty[UserToken])

  def setUserState(token: UserToken): Unit =
    userState.set(Option(token))
    Storage.set(stateName, token)

  def loadUserState(): Unit =
    // clear expired token
    Storage
      .get[UserToken](stateName)
      .filter(_.expires * 1000 <= new Date().getTime())
      .foreach(_ => Storage.remove(stateName))

    // set existing token
    userState.set(Storage.get[UserToken](stateName))

  def clearUserState(): Unit =
    userState.set(Option.empty)
    Storage.remove(stateName)
