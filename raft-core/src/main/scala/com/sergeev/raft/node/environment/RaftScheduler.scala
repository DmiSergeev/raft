package com.sergeev.raft.node.environment

trait RaftScheduler {
  def schedule(interval: Long, task: Runnable): Unit
}

object RaftScheduler extends RaftScheduler {
  def schedule(interval: Long, task: Runnable): Unit = {}
}
