package com.sergeev.raft.node.state

import com.sergeev.raft.node.NodeId

case class RaftCandidateState(override val persistent: RaftPersistentState, override val volatile: RaftCandidateVolatileState)
  extends RaftState[RaftCandidateVolatileState, RaftCandidateState] {

  override def create(persistent: RaftPersistentState, volatile: RaftCandidateVolatileState): RaftCandidateState = RaftCandidateState(persistent, volatile)

  def grantedVotes: Int = volatile.supporters.size

  def addSupporter(supporter: NodeId): RaftCandidateState =
    RaftCandidateState(persistent, volatile.copy(supporters = volatile.supporters + supporter))
}
