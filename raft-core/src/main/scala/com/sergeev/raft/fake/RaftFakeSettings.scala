package com.sergeev.raft.fake

import com.sergeev.raft.node.NodeId
import com.sergeev.raft.util.implicits._

import scala.collection.mutable
import scala.util.Random

class RaftFakeSettings(nodes: List[NodeId], random: Random, defaultRange: Range = Range(1, 4)) {
  val currentPingTime: mutable.Map[(NodeId, NodeId), Range] = {
    var map = mutable.Map.empty[(NodeId, NodeId), Range]
    for {node1 <- nodes
         node2 <- nodes
         if node1 != node2}
      map((node1, node2)) = defaultRange
    map
  }

  def setTime(from: NodeId, to: NodeId, timeRange: Range): Unit = if (from != to) currentPingTime((from, to)) = timeRange

  def setTimesFrom(from: NodeId, timeRange: Range): Unit = for (node <- nodes) setTime(from, node, timeRange)

  def setTimesTo(to: NodeId, timeRange: Range): Unit = for (node <- nodes) setTime(node, to, timeRange)

  def setAllTimes(timeRange: Range): Unit = for (node <- nodes) setTimesFrom(node, timeRange)

  def pingTime(from: NodeId, to: NodeId): Int = currentPingTime((from, to)).rand(random)
}
