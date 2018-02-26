package com.sergeev.raft.fake

import com.sergeev.raft.node.environment.RaftNetworkEndpoint
import com.sergeev.raft.node.message.RaftMessage
import com.sergeev.raft.node.{NodeId, RaftCommand}

class RaftNetworkStub extends RaftNetworkEndpoint {
  override def sendMessage(target: NodeId, message: RaftMessage): Unit = {}

  override def sendClientAck(command: RaftCommand): Unit = {}
}
