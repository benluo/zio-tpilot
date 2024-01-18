package com.rockthejvm.reviewboard.services

import com.rockthejvm.reviewboard.config.{Configs, EmailServiceConfig}
import zio.*

import java.util.Properties
import javax.mail.internet.MimeMessage
import javax.mail.{Authenticator, Message, PasswordAuthentication, Session, Transport}

/**
 * Service for sending emails
 */
trait EmailService:
  /**
   * Send an email from a configured source
   * @param to the recipient address of the email
   * @param subject the subject of the email
   * @param content the content/body of the email
   * @return a task indicating if the email successfully sent
   */
  def sendEmail(to: String, subject: String, content: String): Task[Unit]

  /**
   * Send a password recovery email from a configured source
   * @param to the recipient address of the email
   * @param token the recovery token used to generate a new password
   * @return a task indicating if the email successfully sent
   */
  def sendPasswordRecovery(to: String, token: String): Task[Unit] =
    val subject = "Rock the JVM: Password Recovery"
    val content =
      s"""
         |<div style="
         |  border: 1px solid black;
         |  padding: 20px;
         |  font-family: sans-serif;
         |  line-height: 2;
         |  font-size: 20px;
         |">
         |  <h1>Rock the JVM: Password Recovery</h1>
         |  <p>Your password recovery token is: <strong>$token</strong></p>
         |  <p>:) from Rock the JVM</p>
         |</div>
         |""".stripMargin
    sendEmail(to, subject, content)
end EmailService

/**
 * Implementation of EmailService configured with email host, port, etc.
 * @param config the configuration for how to send emails
 */
class EmailServiceLive private (config: EmailServiceConfig) extends EmailService:
  override def sendEmail(to: String, subject: String, content: String): Task[Unit] =
    for
      props <- propsResource
      session <- createSession(props)
      message <- createMessage(session)("daniel@rockthejvm.com", to, subject, content)
    yield Transport.send(message)

  private val propsResource: Task[Properties] =
    val props = Properties()
    props.put("mail.smtp.auth", true)
    props.put("mail.smtp.starttls.enable", "true")
    props.put("mail.smtp.host", config.host)
    props.put("mail.smtp.port", config.port)
    props.put("mail.smtp.ssl.trust", config.host)
    ZIO.succeed(props)

  private def createSession(props: Properties): Task[Session] =
    ZIO.attempt:
      Session.getInstance(
        props,
        new Authenticator:
          override def getPasswordAuthentication: PasswordAuthentication =
            PasswordAuthentication(config.user, config.pass)
      )

  private def createMessage(session: Session)
                           (from: String, to: String, subject: String, content: String): Task[MimeMessage] =
    val message = MimeMessage(session)
    message.setFrom(from)
    message.setRecipients(Message.RecipientType.TO, to)
    message.setSubject(subject)
    message.setContent(content, "text/html; charset=utf-8")
    ZIO.succeed(message)
end EmailServiceLive

object EmailServiceLive:
  val layer: ZLayer[EmailServiceConfig, Nothing, EmailService] =
    ZLayer:
      ZIO.service[EmailServiceConfig]
        .map(EmailServiceLive(_))

  val configuredLayer: ZLayer[Any, Throwable, EmailService] =
    Configs.makeLayer[EmailServiceConfig]("rockthejvm.email") >>>
    layer
end EmailServiceLive
