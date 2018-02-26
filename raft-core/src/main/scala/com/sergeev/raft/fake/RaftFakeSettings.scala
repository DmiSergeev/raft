package com.sergeev.raft.fake

import com.sergeev.raft.node.NodeId

import scala.collection.mutable
import scala.util.Random

class RaftFakeSettings(val nodes: List[NodeId]) {
  val random: Random = new Random(123456)

  private def initTimes(): mutable.Map[(NodeId, NodeId), Range] = {
    var map = mutable.Map.empty[(NodeId, NodeId), Range]
    for {node1 <- nodes
         node2 <- nodes
         if node1 != node2}
      map((node1, node2)) = Range(0, 1)
    map
  }

  val currentPingTime: mutable.Map[(NodeId, NodeId), Range] = initTimes()


  def setTime(from: NodeId, to: NodeId, timeRange: Range): Unit = if (from != to) currentPingTime((from, to)) = timeRange

  def setTimesFrom(from: NodeId, timeRange: Range): Unit = for (node <- nodes) setTime(from, node, timeRange)

  def setTimesTo(to: NodeId, timeRange: Range): Unit = for (node <- nodes) setTime(node, to, timeRange)

  def setAllTimes(timeRange: Range): Unit = for (node <- nodes) setTimesFrom(node, timeRange)

  def pingTime(from: NodeId, to: NodeId): Int = {
    val range = currentPingTime((from, to))
    range.start + random.nextInt(range.length)
  }
}
