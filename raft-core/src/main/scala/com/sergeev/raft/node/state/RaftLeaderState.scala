package com.sergeev.raft.node.state

import com.sergeev.raft.node._

case class RaftLeaderState(override val persistent: RaftPersistentState, override val volatile: RaftLeaderVolatileState)
  extends RaftState[RaftLeaderVolatileState, RaftLeaderState] {

  override def create(persistent: RaftPersistentState, volatile: RaftLeaderVolatileState): RaftLeaderState = RaftLeaderState(persistent, volatile)

  def prevLogIndex(node: NodeId): RaftIndexer = volatile.nextIndex(node) - 1

  def prevLogTerm(node: NodeId): RaftTerm = logTerm(volatile.nextIndex(node) - 1)

  def logPatch(node: NodeId): Vector[RaftEntry] = persistent.log.drop(volatile.nextIndex(node))

  def initialize(others: List[NodeId]): RaftLeaderState = create(
    persistent,
    volatile.copy(nextIndex = others.map(x ⇒ (x, lastLogIndex)).toMap, matchIndex = others.map(x ⇒ (x, 0)).toMap))

  def appendEntry(command: RaftCommand): RaftLeaderState = create(persistent.copy(log = persistent.log :+ RaftEntry(currentTerm, command)), volatile)

  def decrementNextIndex(node: NodeId): RaftLeaderState =
    create(persistent, volatile.copy(nextIndex = volatile.nextIndex.updated(node, volatile.nextIndex(node) - 1)))

  def updateIndices(approvedNode: NodeId, majority: Int): RaftLeaderState = {
    val matchIndex = volatile.nextIndex(approvedNode)
    val updated = copy(volatile = volatile.copy(
      nextIndex = volatile.nextIndex.updated(approvedNode, matchIndex + 1), matchIndex = volatile.matchIndex.updated(approvedNode, matchIndex)))
    val majorityIndex = volatile.matchIndex.values.toVector.sorted.apply(majority - 2)
    if (volatile.commitIndex < majorityIndex && persistent.log(majorityIndex).term == currentTerm)
      updated.copy(volatile = volatile.copy(commitIndex = majorityIndex))
    else
      updated
  }
}
