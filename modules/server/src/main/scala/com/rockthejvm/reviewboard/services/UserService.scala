package com.rockthejvm.reviewboard.services

import com.rockthejvm.reviewboard.domain.data.{User, UserToken}
import com.rockthejvm.reviewboard.domain.errors.UnauthorizedError
import com.rockthejvm.reviewboard.repositories.{RecoveryTokensRepository, UserRepository}
import zio.*

import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

/** Service for handling user creation, authentication, etc. */
trait UserService:
  /** Register a new user
    * @param email
    *   the email of the new user
    * @param password
    *   the (non-hashed) password of the new user
    * @return
    *   a task containing the newly created user
    */
  def registerUser(email: String, password: String): Task[User]

  /** Check if a user with a given email and password exists
    * @param email
    *   the email to log in with
    * @param password
    *   the password to log in with
    * @return
    *   a task containing a boolean indicating if the email and password are
    *   correct
    */
  def verifyPassword(email: String, password: String): Task[Boolean]

  /** Create a token for a user if their login credentials are correct
    * @param email
    *   the email to log in with
    * @param password
    *   the password to log in with
    * @return
    *   a task containing a UserToken instance if the credentials are correct
    */
  def generateToken(email: String, password: String): Task[UserToken]

  /** Change the password for an existing user
    * @param email
    *   the email address of the user
    * @param oldPassword
    *   the user's current password
    * @param newPassword
    *   the password to change to
    * @return
    *   a task containing the updated user
    */
  def updatePassword(
      email: String,
      oldPassword: String,
      newPassword: String
  ): Task[User]

  /** Delete a user's account
    * @param email
    *   the email of the account
    * @param password
    *   the password of the account
    * @return
    *   a task containing the deleted account
    */
  def deleteUser(email: String, password: String): Task[User]

  /** Send a recovery token email to a user
    * @param email
    *   the user's email to send the recovery email to
    * @return
    *   a task indicating if the email successfully sent
    */
  def sendRecoveryToken(email: String): Task[Unit]

  /** Recover an account by providing an email, recovery token, and new password
    * @param email
    *   the email of the account to recover
    * @param token
    *   a previously generated recovery token
    * @param newPassword
    *   the new password
    * @return
    *   a task containing a boolean indicating if the recovery was successful
    */
  def recoverFromToken(
      email: String,
      token: String,
      newPassword: String
  ): Task[Boolean]
end UserService

/** Implementation of UserService using other services and repo layers
  * @param jwtService
  *   the service for generating/verifying JSON Web Tokens
  * @param emailService
  *   the service for sending recovery token emails
  * @param userRepo
  *   the repo for creating/editing/deleting user accounts
  * @param tokenRepo
  *   the repo for generating/verifying JSON Web Tokens
  */
class UserServiceLive private (
    jwtService: JwtService,
    emailService: EmailService,
    userRepo: UserRepository,
    tokenRepo: RecoveryTokensRepository
) extends UserService:
  override def registerUser(email: String, password: String): Task[User] =
    userRepo.create(
      User(-1L, email, UserServiceLive.Hasher.generateHash(password))
    )

  override def verifyPassword(email: String, password: String): Task[Boolean] =
    verifyUser(email, password).isSuccess

  override def generateToken(email: String, password: String): Task[UserToken] =
    for
      user  <- verifyUser(email, password)
      token <- jwtService.createToken(user)
    yield token

  override def updatePassword(
      email: String,
      oldPassword: String,
      newPassword: String
  ): Task[User] =
    import UserServiceLive.Hasher.generateHash
    for
      user <- verifyUser(email, oldPassword)
      updated <- userRepo.update(
        user.id,
        _.copy(hashedPassword = generateHash(newPassword))
      )
    yield updated

  override def deleteUser(email: String, password: String): Task[User] =
    for
      user    <- verifyUser(email, password)
      deleted <- userRepo.delete(user.id)
    yield deleted

  private def verifyUser(email: String, password: String): Task[User] =
    for
      user <- userRepo
        .getByEmail(email)
        .someOrFail(UnauthorizedError("Invalid email or password."))
      verified <- ZIO.attempt(
        UserServiceLive.Hasher.validateHash(password, user.hashedPassword)
      )
      verifiedUser <- ZIO
        .attempt(user)
        .when(verified)
        .someOrFail(UnauthorizedError("Invalid email or password."))
    yield verifiedUser

  override def sendRecoveryToken(email: String): Task[Unit] =
    tokenRepo
      .getToken(email)
      .flatMap:
        case Some(token) => emailService.sendPasswordRecovery(email, token)
        case None        => ZIO.unit

  override def recoverFromToken(
      email: String,
      token: String,
      newPassword: String
  ): Task[Boolean] =
    import UserServiceLive.Hasher.generateHash
    for
      user <- userRepo
        .getByEmail(email)
        .someOrFail(UnauthorizedError("Invalid email or token."))
      valid <- tokenRepo.checkToken(email, token)
      result <- userRepo
        .update(user.id, _.copy(hashedPassword = generateHash(newPassword)))
        .when(valid)
        .map(_.nonEmpty)
    yield result
end UserServiceLive

object UserServiceLive:
  private type R = UserRepository & RecoveryTokensRepository & EmailService &
    JwtService
  val layer: ZLayer[R, Nothing, UserServiceLive] = ZLayer:
    for
      jwtService   <- ZIO.service[JwtService]
      emailService <- ZIO.service[EmailService]
      userRepo     <- ZIO.service[UserRepository]
      tokenRepo    <- ZIO.service[RecoveryTokensRepository]
    yield UserServiceLive(jwtService, emailService, userRepo, tokenRepo)

  private object Hasher:
    def generateHash(str: String): String =
      val rng  = new SecureRandom()
      val salt = Array.ofDim[Byte](SALT_BYTE_SIZE)
      rng.nextBytes(salt)
      val hashBytes =
        pbkdf2(str.toCharArray, salt, N_ITERATIONS, HASH_BYTE_SIZE)
      s"$N_ITERATIONS:${toHex(salt)}:${toHex(hashBytes)}"

    def validateHash(test: String, hash: String): Boolean =
      val hashSections = hash.split(":")
      val nIters       = hashSections(0).toInt
      val salt         = fromHex(hashSections(1))
      val validHash    = fromHex(hashSections(2))
      val testHash     = pbkdf2(test.toCharArray, salt, nIters, HASH_BYTE_SIZE)
      compareBytes(testHash, validHash)

    private val PBKDF2_ALGORITHM: String = "PBKDF2WithHmacSHA512"
    private val N_ITERATIONS: Int        = 1000
    private val SALT_BYTE_SIZE: Int      = 24
    private val HASH_BYTE_SIZE: Int      = 24
    private val skf = SecretKeyFactory.getInstance(PBKDF2_ALGORITHM)

    private def pbkdf2(
        msg: Array[Char],
        salt: Array[Byte],
        i: Int,
        nBytes: Int
    ): Array[Byte] =
      val keySpec = PBEKeySpec(msg, salt, i, nBytes * 8)
      skf.generateSecret(keySpec).getEncoded

    private def toHex(arr: Array[Byte]): String =
      arr.map("%02X".format(_)).mkString

    private def fromHex(str: String): Array[Byte] =
      str.grouped(2).toArray.map(Integer.parseInt(_, 16).toByte)

    private def compareBytes(a: Array[Byte], b: Array[Byte]): Boolean =
      val diff =
        (0 until (a.length min b.length))
          .foldLeft(a.length ^ b.length): (acc, i) =>
            acc | (a(i) ^ b(i))
      diff == 0
  end Hasher
end UserServiceLive
