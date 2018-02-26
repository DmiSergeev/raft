package com.sergeev.raft.node.environment

trait RaftScheduler {
  def schedule(interval: Int, task: Runnable): Unit
}
