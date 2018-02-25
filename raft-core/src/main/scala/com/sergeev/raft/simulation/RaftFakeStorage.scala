package com.sergeev.raft.simulation

import com.sergeev.raft.node.environment.RaftStorage
import com.sergeev.raft.node.state.RaftPersistentState

class RaftFakeStorage extends RaftStorage {
  var persistentState: RaftPersistentState = RaftPersistentState(-1, None, Vector())

  def save(savedState: RaftPersistentState): Unit = { persistentState = savedState }

  def restore(): RaftPersistentState = persistentState
}
