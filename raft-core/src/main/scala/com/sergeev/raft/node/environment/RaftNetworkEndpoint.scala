package com.sergeev.raft.node.environment

import com.sergeev.raft.node.message.RaftMessage
import com.sergeev.raft.node.{ NodeId, RaftCommand }

trait RaftNetworkEndpoint {
  def sendMessage(target: NodeId, message: RaftMessage): Unit

  def sendClientAck(command: RaftCommand): Unit
}

object RaftNetworkEndpoint extends RaftNetworkEndpoint {
  def sendMessage(target: NodeId, message: RaftMessage): Unit = {}

  def sendClientAck(command: RaftCommand): Unit = {}
}
