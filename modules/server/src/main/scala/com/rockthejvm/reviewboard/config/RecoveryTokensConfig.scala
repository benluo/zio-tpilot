package com.rockthejvm.reviewboard.config

/**
 * Configuration for RecoveryTokensRepository
 *
 * @param duration how long recovery tokens should last before expiring
 */
case class RecoveryTokensConfig(duration: Long)
