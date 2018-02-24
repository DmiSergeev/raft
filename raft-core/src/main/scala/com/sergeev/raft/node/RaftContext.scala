package com.sergeev.raft.node

import org.joda.time.DateTime

trait RaftContext {
  val now: DateTime

  val majority: Int

  val heartbeatTimeout: Int

  val electionTimeout: Int

  val selfId: NodeId

  val others: List[NodeId]

  var senderId: NodeId
}
