package com.sergeev.raft

import com.sergeev.raft.node.message.RaftMessage
import com.sergeev.raft.node.role.RaftRole
import com.sergeev.raft.node.state.{ RaftState, RaftVolatileState }

package object node {
  type NodeId = Long

  type RaftTerm = Long

  type RaftIndexer = Int

  type ProcessingResult[S <: RaftState[_ <: RaftVolatileState[_], S]] = (RaftRole[_ <: RaftState[_ <: RaftVolatileState[_], _]], S, List[(NodeId, RaftMessage)])
}
