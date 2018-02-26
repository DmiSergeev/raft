package com.sergeev.raft.node.message

import com.sergeev.raft.node._

trait RaftMessage

sealed abstract class ExternalTargetMessage() extends RaftMessage

sealed abstract class SelfImmediateMessage() extends RaftMessage

sealed abstract class SelfDeferredMessage(val interval: Int) extends RaftMessage

case class RetryProcessingMessage() extends RaftMessage

case class ClientCommand(command: RaftCommand) extends RaftMessage

case class AppendEntriesRequest(term: RaftTerm, leaderId: NodeId, prevLogIndex: RaftIndexer, prevLogTerm: RaftTerm,
    entries: Vector[RaftEntry], leaderCommit: RaftIndexer) extends ExternalTargetMessage

case class AppendEntriesResponse(term: RaftTerm, success: Boolean) extends ExternalTargetMessage

case class RequestVoteRequest(term: RaftTerm, candidateId: NodeId, lastLogIndex: RaftIndexer, lastLogTerm: RaftTerm) extends ExternalTargetMessage

case class RequestVoteResponse(term: RaftTerm, voteGranted: Boolean) extends ExternalTargetMessage

case class StartUpMessage() extends SelfImmediateMessage

case class SetUpCandidateMessage() extends SelfImmediateMessage

case class SetUpLeaderMessage() extends SelfImmediateMessage

case class IdleTimeoutMessage(override val interval: Int) extends SelfDeferredMessage(interval)

case class ElectionTimeoutMessage(override val interval: Int) extends SelfDeferredMessage(interval)

case class HeartbeatLeaderMessage(override val interval: Int) extends SelfDeferredMessage(interval)

