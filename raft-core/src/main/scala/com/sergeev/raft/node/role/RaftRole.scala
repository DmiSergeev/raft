package com.sergeev.raft.node.role

import com.sergeev.raft.node.message.{ AppendEntriesResponse, RaftMessage, RequestVoteResponse, RetryProcessingMessage }
import com.sergeev.raft.node.state.{ RaftState, RaftVolatileState }
import com.sergeev.raft.node.{ ProcessingResult, RaftContext, RaftTerm, StateHolder }

abstract class RaftRole[S <: RaftState[_ <: RaftVolatileState[_], S]] {
  def initializeState(): S = ???

  def convertState(state: RaftState[_ <: RaftVolatileState[_], _]): StateHolder[S]

  def processIncoming(incoming: RaftMessage, state: S)(context: RaftContext): ProcessingResult[S]

  protected def singleResponse[RS <: RaftState[_ <: RaftVolatileState[_], RS]](role: RaftRole[RS], state: S, response: RaftMessage)(context: RaftContext): ProcessingResult[S] =
    (role, state, List((context.senderId, response)))

  protected def singleToSelf[RS <: RaftState[_ <: RaftVolatileState[_], RS]](role: RaftRole[RS], state: S, response: RaftMessage)(context: RaftContext): ProcessingResult[S] =
    (role, state, List((context.selfId, response)))

  protected def responseAndToSelf[RS <: RaftState[_ <: RaftVolatileState[_], RS]](role: RaftRole[RS], state: S, toSelf: RaftMessage, response: RaftMessage)(context: RaftContext): ProcessingResult[S] =
    (role, state, List((context.selfId, toSelf), (context.senderId, response)))

  protected def processCompetitorAppendEntries(state: S, term: RaftTerm)(context: RaftContext): ProcessingResult[S] = {
    processCompetitorRequest(state, term, term ⇒ AppendEntriesResponse(term, success = false))(context)
  }

  protected def processCompetitorRequestVote(state: S, term: RaftTerm)(context: RaftContext): ProcessingResult[S] = {
    processCompetitorRequest(state, term, term ⇒ RequestVoteResponse(term, voteGranted = false))(context)
  }

  private def processCompetitorRequest(state: S, term: RaftTerm, decline: RaftTerm ⇒ RaftMessage)(context: RaftContext): ProcessingResult[S] = {
    val currentTerm = state.currentTerm
    if (currentTerm < term)
      singleToSelf(RaftFollower, state.updateTerm(term), RetryProcessingMessage())(context)
    else
      singleResponse(this, state, decline(currentTerm))(context)
  }
}
