package com.rockthejvm.reviewboard.domain.data

final case class RecoveryToken(email: String, token: String, expiration: Long)
