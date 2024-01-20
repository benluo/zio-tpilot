package com.rockthejvm.reviewboard.core

import com.rockthejvm.reviewboard.config.BackendClientConfig
import com.rockthejvm.reviewboard.http.endpoints.{
  CompanyEndpoints,
  UserEndpoints
}
import sttp.capabilities
import sttp.capabilities.zio.ZioStreams
import sttp.client3.*
import sttp.client3.impl.zio.FetchZioBackend
import sttp.tapir.Endpoint
import sttp.tapir.client.sttp.SttpClientInterpreter
import zio.*

/** Exception thrown if a secure endpoint is accessed while not logged in */
case class RestrictedEndpointException(msg: String)
    extends RuntimeException(msg)

/** A facade for making requests to tapir endpoints on the backend */
trait BackendClient:
  val company: CompanyEndpoints = new CompanyEndpoints {}
  val user: UserEndpoints       = new UserEndpoints {}

  def endpointRequest[I, E <: Throwable, O](
      endpoint: Endpoint[Unit, I, E, O, Any]
  ): I => Request[Either[E, O], Any]

  def secureEndpointRequest[I, E <: Throwable, O](
      endpoint: Endpoint[String, I, E, O, Any]
  ): String => I => Request[Either[E, O], Any]

  /** make a request to an endpoint
    * @param endpoint
    *   the tapir endpoint to send a request to
    * @param payload
    *   the payload/body to include in the request
    * @tparam I
    *   the type of the payload
    * @tparam E
    *   the error that may occur
    * @tparam O
    *   the successful response type
    * @return
    *   a ZIO task wrapping the response
    */
  def endpointRequestZIO[I, E <: Throwable, O](
      endpoint: Endpoint[Unit, I, E, O, Any]
  )(payload: I): Task[O]

  /** make a request to a secure endpoint
    * @param endpoint
    *   the tapir endpoint to send a request to
    * @param payload
    *   the payload/body to include in the request
    * @tparam I
    *   the type of the payload
    * @tparam E
    *   the error that may occur
    * @tparam O
    *   the successful response type
    * @return
    *   a ZIO task wrapping the response
    */
  def secureEndpointRequestZIO[I, E <: Throwable, O](
      endpoint: Endpoint[String, I, E, O, Any]
  )(payload: I): Task[O]

class BackendClientLive private (
    backend: SttpBackend[zio.Task, ZioStreams & capabilities.WebSockets],
    interpreter: SttpClientInterpreter,
    config: BackendClientConfig
) extends BackendClient:
  override def endpointRequest[I, E <: Throwable, O](
      endpoint: Endpoint[Unit, I, E, O, Any]
  ): I => Request[Either[E, O], Any] =
    interpreter
      .toRequestThrowDecodeFailures(endpoint, config.uri)

  override def secureEndpointRequest[I, E <: Throwable, O](
      endpoint: Endpoint[String, I, E, O, Any]
  ): String => I => Request[Either[E, O], Any] =
    interpreter
      .toSecureRequestThrowDecodeFailures(endpoint, config.uri)

  override def endpointRequestZIO[I, E <: Throwable, O](
      endpoint: Endpoint[Unit, I, E, O, Any]
  )(payload: I): Task[O] =
    backend
      .send(endpointRequest(endpoint)(payload))
      .map(_.body)
      .absolve

  override def secureEndpointRequestZIO[I, E <: Throwable, O](
      endpoint: Endpoint[String, I, E, O, Any]
  )(payload: I): Task[O] =
    for
      token <- tokenOrFail
      response <- backend
        .send(secureEndpointRequest(endpoint)(token)(payload))
        .map(_.body)
        .absolve
    yield response

  private def tokenOrFail: Task[String] =
    ZIO
      .fromOption(Session.getUserState)
      .orElseFail(RestrictedEndpointException("You need to log in"))
      .map(_.token)

object BackendClientLive:
  val layer = ZLayer:
    for
      backend <- ZIO
        .service[SttpBackend[Task, ZioStreams & capabilities.WebSockets]]
      interpreter <- ZIO.service[SttpClientInterpreter]
      config      <- ZIO.service[BackendClientConfig]
    yield BackendClientLive(backend, interpreter, config)

  val configuredLayer: ZLayer[Any, Nothing, BackendClient] =
    ZLayer.succeed(FetchZioBackend()) ++
      ZLayer.succeed(SttpClientInterpreter()) ++
      ZLayer.succeed(BackendClientConfig(Some(uri"http://localhost:8080"))) >>>
      layer
