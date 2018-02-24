package com.sergeev.raft.node.state

import com.sergeev.raft.node.RaftContext

case class RaftFollowerState(override val persistent: RaftPersistentState, override val volatile: RaftFollowerVolatileState)
  extends RaftState[RaftFollowerVolatileState, RaftFollowerState] {

  override def create(persistent: RaftPersistentState, volatile: RaftFollowerVolatileState): RaftFollowerState = RaftFollowerState(persistent, volatile)

  def withUpdatedLastTime(context: RaftContext): RaftFollowerState = copy(volatile = volatile.copy(lastMessageTime = context.now))
}
