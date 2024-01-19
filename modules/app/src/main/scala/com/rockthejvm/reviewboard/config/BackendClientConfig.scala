package com.rockthejvm.reviewboard.config

import sttp.model.Uri

/** Configuration for BackendClient
  * @param uri
  *   the root uri for the backend/api server
  */
case class BackendClientConfig(uri: Option[Uri] = None)
