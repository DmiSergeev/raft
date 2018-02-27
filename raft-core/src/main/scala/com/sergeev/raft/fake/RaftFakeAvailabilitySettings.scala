package com.sergeev.raft.fake

import com.sergeev.raft.node.NodeId
import com.sergeev.raft.util.implicits._

import scala.collection.mutable
import scala.util.Random

class RaftFakeAvailabilitySettings(nodes: List[NodeId], random: Random, defaultRange: Range = Range(1, 4)) {
  val currentPingTime: mutable.Map[(NodeId, NodeId), Range] = {
    var map = mutable.Map.empty[(NodeId, NodeId), Range]
    for {node1 <- nodes
         node2 <- nodes
         if node1 != node2}
      map((node1, node2)) = defaultRange
    map
  }

  def setTime(from: NodeId, to: NodeId, timeRange: Range, mutual: Boolean = true): Unit =
    if (from != to) {
      currentPingTime((from, to)) = timeRange
      if (mutual)
        currentPingTime((to, from)) = timeRange
    }

  def setTimesFrom(from: NodeId, timeRange: Range, mutual: Boolean = true): Unit = for (node <- nodes) setTime(from, node, timeRange, mutual)

  def setTimesTo(to: NodeId, timeRange: Range): Unit = for (node <- nodes) setTime(node, to, timeRange, mutual = false)

  def setAllTimes(timeRange: Range): Unit = for (node <- nodes) setTimesFrom(node, timeRange)


  def setUnavailable(from: NodeId, to: NodeId, mutual: Boolean = true): Unit = setTime(from, to, RaftFakeAvailabilitySettings.unavailable, mutual)

  def setUnavailableFrom(from: NodeId, mutual: Boolean = true): Unit = setTimesFrom(from, RaftFakeAvailabilitySettings.unavailable, mutual)

  def setUnavailableTo(to: NodeId): Unit = setTimesTo(to, RaftFakeAvailabilitySettings.unavailable)

  def setUnavailableAll(): Unit = setAllTimes(RaftFakeAvailabilitySettings.unavailable)


  def resetTime(from: NodeId, to: NodeId, mutual: Boolean = true): Unit = setTime(from, to, defaultRange, mutual)

  def resetTimesFrom(from: NodeId, mutual: Boolean = true): Unit = setTimesFrom(from, defaultRange, mutual)

  def resetTimesTo(to: NodeId): Unit = setTimesTo(to, defaultRange)

  def resetAllTimes(): Unit = setAllTimes(defaultRange)


  def pingTime(from: NodeId, to: NodeId): Int = currentPingTime((from, to)).rand(random)

  def isAvailable(from: NodeId, to: NodeId): Boolean = currentPingTime((from, to)) != RaftFakeAvailabilitySettings.unavailable
}

private object RaftFakeAvailabilitySettings {
  val unavailable: Range = Range(Int.MaxValue, Int.MaxValue)
}