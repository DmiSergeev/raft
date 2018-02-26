package com.sergeev.raft.fake

import com.sergeev.raft.node.{NodeId, RaftCommand, RaftInstance}
import com.sergeev.raft.node.environment.{RaftNetworkEndpoint, RaftScheduler}
import com.sergeev.raft.node.message.RaftMessage

import scala.collection.mutable

class RaftFakeNetwork(selfId: NodeId, routing: mutable.Map[NodeId, RaftInstance],
                      scheduler: RaftScheduler, settings: RaftFakeSettings) extends RaftNetworkEndpoint {
  override def sendMessage(target: NodeId, message: RaftMessage): Unit =
    scheduler.schedule(settings.pingTime(selfId, target), () => routing(target).processNodeMessage(selfId, message))

  def processMessage(sender: NodeId, message: RaftMessage): Unit = routing(selfId).processNodeMessage(sender, message)

  override def sendClientAck(command: RaftCommand): Unit = println("Client ack: " + command)
}
