package com.rockthejvm.reviewboard.core

import com.raquo.laminar.api.L.{EventBus, EventStream}
import sttp.tapir.Endpoint
import zio.*

/** interop-layer for using tapir/ZIO inside laminar components
  */
object ZJS:
  val useBackend: ZIO.ServiceWithZIOPartiallyApplied[BackendClient] =
    ZIO.serviceWithZIO[BackendClient]

  extension [E <: Throwable, A](zio: ZIO[BackendClient, E, A])
    /** Run the ZIO effect and emit the result to an Airstream EventBus
      * @param eventBus
      *   the event bus to emit to
      */
    def emitTo(eventBus: EventBus[A]): Unit =
      Unsafe.unsafe:
        implicit unsafe =>
          Runtime.default.unsafe.fork:
              zio
                .tap(result => ZIO.attempt(eventBus.emit(result)))
                .provide(BackendClientLive.configuredLayer)

    /** run the ZIO effect as an async js call
      * @return
      *   the future/promise of the result
      */
    def runJs: CancelableFuture[A] =
      Unsafe.unsafe:
        implicit unsafe =>
          Runtime.default.unsafe
            .runToFuture(zio.provide(BackendClientLive.configuredLayer))

    def toEventStream: EventStream[A] =
      val bus = EventBus[A]()
      emitTo(bus)
      bus.events
  end extension

  extension [I, E <: Throwable, O](endpoint: Endpoint[Unit, I, E, O, Any])
    /** make a request to the endpoint with a given payload
      * @param payload
      *   the request payload
      * @return
      *   a ZIO task wrapping the response
      */
    def apply(payload: I): Task[O] =
      ZIO
        .service[BackendClient]
        .flatMap(_.endpointRequestZIO(endpoint)(payload))
        .provide(BackendClientLive.configuredLayer)
