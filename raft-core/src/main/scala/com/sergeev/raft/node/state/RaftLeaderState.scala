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
    volatile.copy(nextIndex = others.map(x ⇒ (x, lastLogIndex + 1)).toMap, matchIndex = others.map(x ⇒ (x, -1)).toMap))

  def appendEntry(command: RaftCommand): RaftLeaderState = create(persistent.copy(log = persistent.log :+ RaftEntry(currentTerm, command)), volatile)

  def decrementNextIndex(node: NodeId): RaftLeaderState =
    create(persistent, volatile.copy(nextIndex = volatile.nextIndex.updated(node, volatile.nextIndex(node) - 1)))

  def updateIndices(approvedNode: NodeId, majority: Int): RaftLeaderState = {
    val newMatchIndex = math.min(volatile.nextIndex(approvedNode), lastLogIndex)
    val updated = copy(volatile = volatile.copy(
      nextIndex = volatile.nextIndex.updated(approvedNode, lastLogIndex + 1), matchIndex = volatile.matchIndex.updated(approvedNode, newMatchIndex)))
    val majorityIndex = updated.volatile.matchIndex.values.toVector.sorted.reverse.apply(majority - 2)
    if (volatile.commitIndex < majorityIndex && logTerm(majorityIndex) == currentTerm)
      updated.copy(volatile = updated.volatile.copy(commitIndex = majorityIndex))
    else
      updated
  }
}
