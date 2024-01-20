package com.rockthejvm.reviewboard.config

/** Configuration for the email service
  *
  * @param host
  *   the mail.smtp.host property
  * @param port
  *   the mail.smtp.port property
  * @param user
  *   the session authenticator user
  * @param pass
  *   the session authenticator password
  */
final case class EmailServiceConfig(
    host: String,
    port: Int,
    user: String,
    pass: String
)
