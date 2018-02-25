package com.sergeev.raft.node.environment

import com.sergeev.raft.node.state.RaftPersistentState

trait RaftStorage {
  def save(savedState: RaftPersistentState): Unit

  def restore(): RaftPersistentState
}
