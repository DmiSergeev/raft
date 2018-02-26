package com.sergeev.raft.node.environment

import org.joda.time.DateTime

trait RaftTimeProvider {
  def now(): DateTime
}

object RaftTimeProvider extends RaftTimeProvider {
  def now(): DateTime = DateTime.now()
}

case class RaftFixedTimeProvider(time: DateTime) extends RaftTimeProvider {
  def now(): DateTime = time
}
