package com.sergeev.raft.node

import com.sergeev.raft.node.environment.RaftTimeProvider
import org.joda.time.DateTime

trait RaftContext {
  def majority: Int

  def heartbeatTimeout: Int

  def electionTimeout: Int

  def selfId: NodeId

  def others: List[NodeId]

  def now: DateTime

  def senderId: NodeId
}

case class RaftContextImpl(timeProvider: RaftTimeProvider, majorityValue: Int,
                       heartbeatTimeoutValue: Int, electionTimeoutValue: Int,
                       selfIdOption: Option[NodeId] = None, othersOption: Option[List[NodeId]] = None,
                       senderIdOption: Option[NodeId] = None) extends RaftContext {
  override def now: DateTime = timeProvider.now()

  override def majority: Int = majorityValue

  override def heartbeatTimeout: Int = heartbeatTimeoutValue

  override def electionTimeout: Int = electionTimeoutValue

  override def selfId: NodeId = selfIdOption.getOrElse(???)

  override def others: List[NodeId] = othersOption.getOrElse(???)

  override def senderId: NodeId = senderIdOption.getOrElse(???)
}
