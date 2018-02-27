package com.sergeev.raft.node.role

import com.sergeev.raft.node.message._
import com.sergeev.raft.node.state._
import com.sergeev.raft.node.{ProcessingResult, RaftContext, StateHolder}
import org.joda.time.DateTime

object RaftFollower extends RaftRole[RaftFollowerState] {
  override def shortName: String = "FLW"

  override def initializeState(raftPersistentState: RaftPersistentState): RaftFollowerState =
    RaftFollowerState(raftPersistentState, RaftFollowerVolatileState(-1, new DateTime(0)))

  override def convertState(state: RaftState[_ <: RaftVolatileState[_], _]): StateHolder[RaftFollowerState] =
    state match {
      case followerState: RaftFollowerState => StateHolder(this, followerState)
      case _ => StateHolder(this, RaftFollowerState(state.persistent, RaftFollowerVolatileState(state.volatile.commitIndex, new DateTime(0))))
    }

  override def processIncoming(incoming: RaftMessage, state: RaftFollowerState)(context: RaftContext): ProcessingResult[RaftFollowerState] = {
    def responseWithTimeout(state: RaftFollowerState, response: RaftMessage): ProcessingResult[RaftFollowerState] =
      responseAndToSelf(RaftFollower, state.withUpdatedLastTime(context), IdleTimeoutMessage(context.electionTimeout), response)(context)

    def responseWithoutTimeout(state: RaftFollowerState, response: RaftMessage): ProcessingResult[RaftFollowerState] =
      singleResponse(RaftFollower, state, response)(context)

    incoming match {
      case StartUpMessage() ⇒ singleToSelf(this, state, IdleTimeoutMessage(context.electionTimeout))(context)

      case IdleTimeoutMessage(_) ⇒
        if (state.volatile.lastMessageTime.plusMillis(context.minimumElectionTimeout) isAfter context.now)
          (this, state, Nil)
        else
          singleToSelf(RaftCandidate, state, SetUpCandidateMessage())(context)

      case AppendEntriesRequest(term, leaderId, prevLogIndex, prevLogTerm, entries, leaderCommit) ⇒
        // 1. Reply false if term < currentTerm
        if (term < state.currentTerm)
          return responseWithoutTimeout(state, AppendEntriesResponse(state.currentTerm, success = false))

        // 2. Reply false if log doesn’t contain an entry at prevLogIndex whose term matches prevLogTerm [consistency check protocol]
        // note: (log entry term > prevLogTerm) means illegal state
        val updatedState = state.updateTerm(term)
        if (updatedState.isStaleWrtLeader(prevLogIndex, prevLogTerm))
          return responseWithTimeout(updatedState, AppendEntriesResponse(updatedState.currentTerm, success = false))

        // 3. If an existing entry conflicts with a new one (same index but different terms),
        // delete the existing entry and all that follow it
        // 4. Append any new entries not already in the log
        // 5. If leaderCommit > commitIndex, set commitIndex = min(leaderCommit, index of last new entry)
        val patchedState = updatedState.patchLog(prevLogIndex, entries, leaderCommit)
        responseWithTimeout(patchedState, AppendEntriesResponse(updatedState.currentTerm, success = true))

      case RequestVoteRequest(term, candidateId, lastLogIndex, lastLogTerm) ⇒
        // 1. Reply false if term < currentTerm
        if (term < state.currentTerm)
          return responseWithoutTimeout(state, RequestVoteResponse(state.currentTerm, voteGranted = false))

        // 2. If votedFor is null or candidateId, and candidate’s log is at least as up-to-date as receiver’s log, grant vote
        // If the logs have last entries with different terms, then the log with the later term is more up-to-date.
        // If the logs end with the same term, then whichever log is longer is more up-to-date
        val updatedState = state.updateTerm(term).tryVoteFor(candidateId, lastLogIndex, lastLogTerm)
        responseWithTimeout(updatedState, RequestVoteResponse(state.currentTerm, updatedState.isVoteFor(candidateId)))

      case _ => (this, state, Nil)
    }
  }
}
