package com.rockthejvm.reviewboard.repositories

import zio.*

trait BaseRepository[A]:
  def create(item: A): Task[A]
  def update(id: Long, op: A => A): Task[A]
  def delete(id: Long): Task[A]
  def getById(id: Long): Task[Option[A]]
  def getAll: Task[List[A]]
