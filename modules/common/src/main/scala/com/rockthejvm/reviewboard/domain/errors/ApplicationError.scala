package com.rockthejvm.reviewboard.domain.errors

class ApplicationError(msg: String) extends Throwable(msg)

case class UnauthorizedError(msg: String) extends ApplicationError(msg)
case class NotFoundError(msg: String) extends ApplicationError(msg)