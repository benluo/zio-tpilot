package com.rockthejvm.reviewboard.repositories

import io.getquill.SnakeCase
import io.getquill.jdbczio.Quill
import zio.*

import javax.sql.DataSource

/** Base data access functionality
  * @tparam A
  *   the domain/model type representing rows in the db table
  */
trait Repository[A]:
  /** Insert an item in the table
    * @param item
    *   the item to insert
    * @return
    *   a task containing the inserted item
    */
  def create(item: A): Task[A]

  /** Update an existing item in the table by invoking a function on it
    * @param id
    *   the id of the item to update
    * @param op
    *   the transformer function to invoke on the existing item
    * @return
    *   a task containing the result of invoking op on the item
    */
  def update(id: Long, op: A => A): Task[A]

  /** Delete an existing item in the table
    * @param id
    *   the id of the item to delete
    * @return
    *   a task containing the deleted item
    */
  def delete(id: Long): Task[A]

  /** Get an item by its id/primary-key
    * @param id
    *   the id to search with
    * @return
    *   a task containing an option of the found item
    */
  def getById(id: Long): Task[Option[A]]

  /** Get all items in the table
    * @return
    *   all items found
    */
  def getAll: Task[List[A]]
end Repository

object Repository:
  def quillLayer: ZLayer[DataSource, Nothing, Quill.Postgres[SnakeCase.type]] =
    Quill.Postgres.fromNamingStrategy(SnakeCase)

  def dataSourceLayer: ZLayer[Any, Throwable, DataSource] =
    Quill.DataSource.fromPrefix("rockthejvm.db")

  val dataLayer: ZLayer[Any, Throwable, Quill.Postgres[SnakeCase.type]] =
    dataSourceLayer >>> quillLayer
