package com.sergeev.raft.fake.discrete

import com.sergeev.raft.node.environment.{RaftScheduler, RaftTimeProvider}
import org.joda.time.DateTime

import scala.collection.mutable

class RaftFakeDiscreteScheduler() extends RaftScheduler with RaftTimeProvider {
  implicit def orderingByTime[A <: (DateTime, Runnable)]: Ordering[A] = Ordering.by(e => e._1.getMillis)

  val executionQueue: mutable.PriorityQueue[(DateTime, Runnable)] = new mutable.PriorityQueue[(DateTime, Runnable)]()

  private var currentTime: DateTime = new DateTime(0)

  def update(time: DateTime): Unit = {
    while (!(executionQueue.head._1 isAfter time)) {
      val (executionTime, task) = executionQueue.dequeue()
      currentTime = executionTime
      task.run()
    }
    currentTime = time
  }

  override def schedule(interval: Int, task: Runnable): Unit = executionQueue.enqueue((currentTime.plusMillis(interval), task))

  override def now(): DateTime = currentTime
}
