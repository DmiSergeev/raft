package com.sergeev.raft.fake.discrete

import com.sergeev.raft.fake._
import com.sergeev.raft.node.environment.RaftStorage
import com.sergeev.raft.node._

import scala.collection.mutable

object RaftDiscreteSimulator extends RaftSimulator {

  def initialize(nodeCount: Int = 5, heartbeatTimeout: Int = 100, electionTimeout: Int = 2000): Unit = {
    val majority = (nodeCount + 1) / 2
    val nodes = Range(1, 1 + nodeCount).toList.map(x => x.toLong)
    val settings = new RaftFakeSettings(nodes)
    val scheduler = new RaftFakeDiscreteScheduler()
    val contextPrototype = RaftContextImpl(scheduler, majority, heartbeatTimeout, electionTimeout)

    val routing = mutable.Map[NodeId, RaftInstance]()
    val nodeEnvironment = nodes.map(node => (node, {
      val context = contextPrototype.copy(selfIdOption = Some(node), othersOption = Some(nodes diff List(node)))
      val network = new RaftFakeNetwork(node, routing, scheduler, settings)
      val storage = new RaftFakeStorage()
      val instance = new RaftRouter(context, network, scheduler, storage)
      RaftNodeFakeEnvironment(context, network, storage, instance)
    })).toMap
    for (node <- nodes)
      routing(node) = nodeEnvironment(node).instance
  }

}

case class RaftNodeFakeEnvironment(context: RaftContext, network: RaftFakeNetwork, storage: RaftStorage, instance: RaftInstance)
