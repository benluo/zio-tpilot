package com.rockthejvm.reviewboard.syntax

import zio.*
import zio.test.*

/** extension method to make zio assertions easier */
extension [R, E, A](zio: ZIO[R, E, A])
  def assert(name: String = "test assertion")(pred: (=> A) => Boolean): ZIO[R, E, TestResult] =
    assertZIO(zio)(Assertion.assertion(name)(pred))
