package com.sergeev.raft.node.state

import com.sergeev.raft.node.{ NodeId, RaftEntry, RaftTerm }

case class RaftPersistentState(currentTerm: RaftTerm, votedFor: Option[NodeId], log: Vector[RaftEntry])
