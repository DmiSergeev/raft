package com.sergeev.raft.node.state

import org.joda.time.DateTime
import com.sergeev.raft.node.{ NodeId, RaftIndexer }

abstract class RaftVolatileState[V <: RaftVolatileState[V]] {
  val commitIndex: RaftIndexer

  def updateIndex(leaderCommit: RaftIndexer, lastNewIndex: RaftIndexer): V =
    if (leaderCommit > commitIndex)
      create(math.min(leaderCommit, lastNewIndex))
    else
      create()

  protected def create(commitIndex: RaftIndexer = commitIndex): V
}

case class RaftFollowerVolatileState(override val commitIndex: RaftIndexer, lastMessageTime: DateTime) extends RaftVolatileState[RaftFollowerVolatileState] {
  override protected def create(commitIndex: RaftIndexer): RaftFollowerVolatileState =
    RaftFollowerVolatileState(commitIndex, lastMessageTime)
}

case class RaftCandidateVolatileState(override val commitIndex: RaftIndexer, supporters: Set[NodeId]) extends RaftVolatileState[RaftCandidateVolatileState] {
  override protected def create(commitIndex: RaftIndexer): RaftCandidateVolatileState =
    RaftCandidateVolatileState(commitIndex, supporters)
}

case class RaftLeaderVolatileState(override val commitIndex: RaftIndexer, nextIndex: Map[NodeId, RaftIndexer], matchIndex: Map[NodeId, RaftIndexer])
  extends RaftVolatileState[RaftLeaderVolatileState] {
  override protected def create(commitIndex: RaftIndexer): RaftLeaderVolatileState =
    RaftLeaderVolatileState(commitIndex, nextIndex, matchIndex)
}
