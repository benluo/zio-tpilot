package com.rockthejvm.reviewboard.http.controllers

import sttp.tapir.server.ServerEndpoint
import zio.Task

/** a controller that implements handling logic for a list of endpoints */
trait Controller:
  /** a list of all routes implemented by this controller */
  val routes: List[ServerEndpoint[Any, Task]]
