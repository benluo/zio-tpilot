package com.rockthejvm.reviewboard.config

import com.typesafe.config.ConfigFactory
import zio.*
import zio.config.*
import zio.config.magnolia.*
import zio.config.typesafe.TypesafeConfig

/** Wrapper over TypesafeConfig to read config files and create ZLayers from
  * them
  */
object Configs:
  /** provide a Z-Layer from a config file
    * @param path
    *   the path in application.config to parse
    * @tparam C
    *   the case class to parse the contents of the config file at path as
    * @return
    *   a Z-Layer for C
    */
  def makeLayer[C : Descriptor : Tag](path: String): ZLayer[Any, Throwable, C] =
    TypesafeConfig.fromTypesafeConfig(
      ZIO.attempt(ConfigFactory.load().getConfig(path)),
      descriptor[C]
    )
