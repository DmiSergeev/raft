package com.sergeev.raft.node

import com.sergeev.raft.node.environment.RaftTimeProvider
import com.sergeev.raft.util.implicits._
import org.joda.time.DateTime

import scala.util.Random

trait RaftContext {
  def majority: Int

  def heartbeatTimeout: Int

  def minimumElectionTimeout: Int

  def electionTimeout: Int

  def selfId: NodeId

  def others: List[NodeId]

  def now: DateTime

  def senderId: NodeId
}

case class RaftContextImpl(timeProvider: RaftTimeProvider, majorityValue: Int,
                           heartbeatTimeoutRange: Range, electionTimeoutRange: Range,
                           selfIdOption: Option[NodeId] = None, othersOption: Option[List[NodeId]] = None,
                           senderIdOption: Option[NodeId] = None, random: Random = new Random(123456)) extends RaftContext {
  override def now: DateTime = timeProvider.now()

  override def majority: Int = majorityValue

  override def heartbeatTimeout: Int = heartbeatTimeoutRange.rand(random)

  override def minimumElectionTimeout: Int = electionTimeoutRange.start

  override def electionTimeout: Int = electionTimeoutRange.rand(random)

  override def selfId: NodeId = selfIdOption.getOrElse(???)

  override def others: List[NodeId] = othersOption.getOrElse(???)

  override def senderId: NodeId = senderIdOption.getOrElse(???)
}

