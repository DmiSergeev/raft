
package com.sergeev.raft.node.role

import com.sergeev.raft.fake.discrete.RaftFakeDiscreteScheduler
import com.sergeev.raft.fake.{RaftFakeStorage, RaftNetworkStub}
import com.sergeev.raft.node.environment._
import com.sergeev.raft.node.message.{AppendEntriesRequest, AppendEntriesResponse, IdleTimeoutMessage}
import com.sergeev.raft.node.state.{RaftFollowerState, RaftFollowerVolatileState, RaftPersistentState}
import com.sergeev.raft.node.{RaftContext, RaftContextImpl, RaftRouter}
import org.joda.time.DateTime
import org.scalatest.{Matchers, WordSpec}

class RaftFollowerSpec extends WordSpec with Matchers {
  "processIncoming" should {
    "respond correctly" when {
      "term is outdated" in {
        val context: RaftContext = RaftContextImpl(RaftFixedTimeProvider(new DateTime(0)), 3, Range(0, 1), Range(0, 1), Some(0), Some(Nil), Some(1))
        val request = AppendEntriesRequest(0, 0, 0, 0, Vector(), 0)
        val state = RaftFollowerState(RaftPersistentState(1, None, Vector()), RaftFollowerVolatileState(0, new DateTime(0)))

        RaftFollower.processIncoming(request, state)(context) shouldBe
          (RaftFollower, state, List((0, IdleTimeoutMessage(0)), (1, AppendEntriesResponse(1, success = false))))
      }
    }
  }
  "stateHolder" should {
    "work correctly" when {
      "always" in {
        val context = RaftContextImpl(RaftFixedTimeProvider(new DateTime(0)), 3, Range(0, 1), Range(0, 1), Some(0), Some(Nil), Some(1))
        val instance = new RaftRouter(context, new RaftNetworkStub(), new RaftFakeDiscreteScheduler(), new RaftFakeStorage)

        val request = AppendEntriesRequest(0, 0, 0, 0, Vector(), 0)
        val state = RaftFollowerState(RaftPersistentState(1, None, Vector()), RaftFollowerVolatileState(0, new DateTime(0)))

        instance.processNodeMessage(1, request) // shouldBe (RaftFollower, state, List((0, IdleTimeoutMessage(0)), (1, AppendEntriesResponse(1, success = false))))
      }
    }
  }
}
