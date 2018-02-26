package com.sergeev.raft.node.state

import com.sergeev.raft.node._

import scala.math.Ordering.Implicits._

abstract class RaftState[V <: RaftVolatileState[V], T <: RaftState[V, T]] {
  val persistent: RaftPersistentState
  val volatile: V

  private def canVoteFor(candidate: NodeId): Boolean = persistent.votedFor.getOrElse(candidate) == candidate

  private def hasEntries: Boolean = persistent.log.nonEmpty

  def lastLogIndex: RaftIndexer = persistent.log.length - 1

  def lastLogTerm: RaftTerm = if (hasEntries) persistent.log(lastLogIndex).term else -1

  // TODO: rewrite
  def logTerm(index: RaftIndexer): RaftTerm =
    if (persistent.log.indices.contains(index)) persistent.log(index).term else -1

  def isStaleWrtLeader(prevLogIndex: RaftIndexer, prevLogTerm: RaftTerm): Boolean =
    lastLogIndex < prevLogIndex || persistent.log(prevLogIndex).term < prevLogTerm

  def isVoteFor(candidate: NodeId): Boolean = persistent.votedFor.contains(candidate)

  def currentTerm: RaftTerm = persistent.currentTerm

  def appliedCommands(oldState: RaftState[_ <: RaftVolatileState[_], _]): List[RaftCommand] =
    persistent.log.slice(oldState.volatile.commitIndex, this.volatile.commitIndex).map(entry ⇒ entry.command).toList

  def create(persistent: RaftPersistentState = persistent, volatile: V = volatile): T

  def updateTerm(term: RaftTerm): T =
    if (term > currentTerm)
      create(persistent.copy(currentTerm = term, votedFor = None))
    else
      create()

  def incrementTerm: T = create(persistent.copy(currentTerm = persistent.currentTerm + 1))

  def tryVoteFor(candidate: NodeId, lastCandidateLogIndex: RaftIndexer, lastCandidateLogTerm: RaftTerm): T =
    if (canVoteFor(candidate) && (lastLogTerm, lastLogIndex) <= (lastCandidateLogTerm, lastCandidateLogIndex))
      create(persistent.copy(votedFor = Some(candidate)))
    else
      create()

  def voteFor(candidate: NodeId): T = create(persistent.copy(votedFor = Some(candidate)))

  def patchLog(prevLogIndex: RaftIndexer, entries: Vector[RaftEntry], leaderCommit: RaftIndexer): T = {
    val lastNewIndex = prevLogIndex + entries.length
    val conflicted = persistent.log.zipWithIndex
      .exists { case (entry, index) ⇒ index > prevLogIndex && index <= lastNewIndex && entry.term != entries(index - (prevLogIndex + 1)).term }
    val patchedLog = persistent.log.take(prevLogIndex + 1) ++
      entries ++
      (if (conflicted) Nil else persistent.log.drop(lastNewIndex + 1))

    create(persistent.copy(log = patchedLog), volatile.updateIndex(leaderCommit, lastNewIndex))
  }
}
