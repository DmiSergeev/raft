package com.sergeev.raft.node.role

import com.sergeev.raft.node.message._
import com.sergeev.raft.node.state._
import com.sergeev.raft.node.{ ProcessingResult, RaftContext, StateHolder }

object RaftCandidate extends RaftRole[RaftCandidateState] {
  override def shortName: String = "CND"

  override def convertState(state: RaftState[_ <: RaftVolatileState[_], _]): StateHolder[RaftCandidateState] =
    StateHolder(this, RaftCandidateState(state.persistent, RaftCandidateVolatileState(state.volatile.commitIndex, Set())))

  override def processIncoming(incoming: RaftMessage, state: RaftCandidateState)(context: RaftContext): ProcessingResult[RaftCandidateState] =
    incoming match {
      case SetUpCandidateMessage() ⇒
        val updatedState = state.incrementTerm.voteFor(context.selfId)
        val messages = (
          context.selfId,
          ElectionTimeoutMessage(context.electionTimeout)) ::
          context.others.map(node ⇒ (node, RequestVoteRequest(
            updatedState.currentTerm, context.selfId, updatedState.lastLogIndex, updatedState.lastLogTerm)))
        (RaftCandidate, updatedState, messages)

      case ElectionTimeoutMessage(_) ⇒ singleToSelf(this, state, SetUpCandidateMessage())(context)

      case RequestVoteResponse(term, voteGranted) ⇒
        val updatedState = state.updateTerm(term)
        if (!updatedState.isVoteFor(context.selfId))
          (RaftFollower, updatedState, Nil)
        else if (!voteGranted)
          (this, updatedState, Nil)
        else {
          val updatedWithSupporter = updatedState.addSupporter(context.senderId)
          if (updatedWithSupporter.grantedVotes < context.majority)
            (this, updatedWithSupporter, Nil)
          else
            singleToSelf(RaftLeader, updatedWithSupporter, SetUpLeaderMessage())(context)
        }

      case AppendEntriesRequest(term, _, _, _, _, _) ⇒ processCompetitorAppendEntries(state, term)(context)

      case RequestVoteRequest(term, _, _, _)         ⇒ processCompetitorRequestVote(state, term)(context)

      case _ => (this, state, Nil)
    }
}
