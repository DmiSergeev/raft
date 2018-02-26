package com.sergeev.raft.fake.discrete

import com.sergeev.raft.fake._
import com.sergeev.raft.node._
import com.sergeev.raft.node.environment.RaftStorage
import com.sergeev.raft.node.role.RaftLeader
import org.joda.time.DateTime

import scala.collection.mutable
import scala.util.Random

class RaftDiscreteSimulator(nodeCount: Int = 5,
                            heartbeatTimeoutRange: Range = Range(90, 110),
                            electionTimeoutRange: Range = Range(1900, 2100),
                            random: Random = new Random(123456)) extends RaftSimulator {
  val majority: Int = (nodeCount + 1) / 2
  val nodes: List[NodeId] = Range(1, 1 + nodeCount).toList.map(x => x.toLong)
  val settings: RaftFakeSettings = new RaftFakeSettings(nodes, random)
  val scheduler: RaftFakeDiscreteScheduler = new RaftFakeDiscreteScheduler()
  val contextPrototype: RaftContextImpl = RaftContextImpl(scheduler, majority, heartbeatTimeoutRange, electionTimeoutRange, random = random)
  val routing: mutable.Map[NodeId, RaftInstance] = mutable.Map[NodeId, RaftInstance]()
  val nodeEnvironment: Map[NodeId, RaftNodeFakeEnvironment] = nodes.map(node => (node, {
    val context = contextPrototype.copy(selfIdOption = Some(node), othersOption = Some(nodes diff List(node)))
    val network = new RaftFakeNetwork(node, routing, scheduler, settings)
    val storage = new RaftFakeStorage()
    val instance = new RaftRouter(context, network, scheduler, storage)
    RaftNodeFakeEnvironment(context, network, storage, instance)
  })).toMap

  for (node <- nodes)
    routing(node) = nodeEnvironment(node).instance

  def start(): Unit = for (env <- nodeEnvironment.values) env.instance.start()

  def sendToLeader(command: RaftCommand): Boolean = {
    val optionalLeader = nodeEnvironment.values.find(env => env.instance.currentRole == RaftLeader).map(env => env.instance)
    if (optionalLeader.nonEmpty)
      optionalLeader.get.processClientCommand(command)
    optionalLeader.nonEmpty
  }

  def updateTime(time: Long): Unit = scheduler.update(new DateTime(time))
}

case class RaftNodeFakeEnvironment(context: RaftContext, network: RaftFakeNetwork, storage: RaftStorage, instance: RaftInstance)
