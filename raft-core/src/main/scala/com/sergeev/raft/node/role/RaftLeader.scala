package com.sergeev.raft.node.role

import com.sergeev.raft.node.message._
import com.sergeev.raft.node.state._
import com.sergeev.raft.node.{ NodeId, ProcessingResult, RaftContext, StateHolder }

object RaftLeader extends RaftRole[RaftLeaderState] {
  override def convertState(state: RaftState[_ <: RaftVolatileState[_], _]): StateHolder[RaftLeaderState] =
    StateHolder(this, RaftLeaderState(state.persistent, RaftLeaderVolatileState(state.volatile.commitIndex, Map(), Map())))

  override def processIncoming(incoming: RaftMessage, state: RaftLeaderState)(context: RaftContext): ProcessingResult[RaftLeaderState] = {
    def makeAppendEntriesForNode(node: NodeId, state: RaftLeaderState) = (node, AppendEntriesRequest(state.currentTerm, context.selfId,
      state.prevLogIndex(node), state.prevLogTerm(node), state.logPatch(node), state.volatile.commitIndex))

    def makeAppendEntriesForAll(state: RaftLeaderState) = context.others.map(node ⇒ makeAppendEntriesForNode(node, state))

    incoming match {
      case SetUpLeaderMessage() ⇒
        val updatedState = state.initialize(context.others)
        val messages = (context.selfId, HeartbeatLeaderMessage(context.heartbeatTimeout)) :: makeAppendEntriesForAll(updatedState)
        (this, updatedState, messages)

      case HeartbeatLeaderMessage(_) ⇒
        val messages = (context.selfId, HeartbeatLeaderMessage(context.heartbeatTimeout)) :: makeAppendEntriesForAll(state)
        (this, state, messages)

      case ClientCommand(command) ⇒
        val updatedState = state.appendEntry(command)
        (this, updatedState, makeAppendEntriesForAll(updatedState))

      case AppendEntriesResponse(term, success) ⇒
        val currentTerm = state.currentTerm
        if (currentTerm < term)
          singleToSelf(RaftFollower, state.updateTerm(term), StartUpMessage())(context)
        else if (!success) {
          val correctedState = state.decrementNextIndex(context.senderId)
          (this, correctedState, List(makeAppendEntriesForNode(context.senderId, correctedState)))
        } else
          (this, state.updateIndices(context.senderId, context.majority), Nil)

      case AppendEntriesRequest(term, _, _, _, _, _) ⇒ processCompetitorAppendEntries(state, term)(context)

      case RequestVoteRequest(term, _, _, _)         ⇒ processCompetitorRequestVote(state, term)(context)
    }
  }
}
