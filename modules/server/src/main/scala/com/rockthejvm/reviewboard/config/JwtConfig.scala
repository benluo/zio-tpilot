package com.rockthejvm.reviewboard.config

/**
 * configuration for JwtService
 * @param secret the JWT secret
 * @param ttl time-to-live; how long tokens should persist before expiring
 */
case class JwtConfig(secret: String, ttl: Long)
