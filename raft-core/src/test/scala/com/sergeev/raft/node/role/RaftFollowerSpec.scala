
package com.sergeev.raft.node.role

import org.joda.time.DateTime
import org.scalatest.{ Matchers, WordSpec }
import com.sergeev.raft.node.environment.{ RaftNetworkEndpoint, RaftScheduler }
import com.sergeev.raft.node.{ NodeId, RaftContext, RaftRouterImpl }
import com.sergeev.raft.node.message.{ AppendEntriesRequest, AppendEntriesResponse, ElectionTimeoutMessage, IdleTimeoutMessage }
import com.sergeev.raft.node.state.{ RaftFollowerState, RaftFollowerVolatileState, RaftPersistentState }

class RaftFollowerSpec extends WordSpec with Matchers {
  "processIncoming" should {
    "respond correctly" when {
      "term is outdated" in {
        val context: RaftContext = new RaftContext {
          override val now: DateTime = new DateTime(0)
          override var senderId: NodeId = 1
          override val selfId: NodeId = 0
          override val majority: Int = 0
          override val others: List[NodeId] = Nil
          override val heartbeatTimeout: Int = 0
          override val electionTimeout: Int = 0
        }
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
        val context: RaftContext = new RaftContext {
          override val now: DateTime = new DateTime(0)
          override var senderId: NodeId = 1
          override val selfId: NodeId = 0
          override val majority: Int = 0
          override val others: List[NodeId] = Nil
          override val heartbeatTimeout: Int = 0
          override val electionTimeout: Int = 0
        }
        val router = new RaftRouterImpl(context, RaftNetworkEndpoint, RaftScheduler)

        val request = AppendEntriesRequest(0, 0, 0, 0, Vector(), 0)
        val state = RaftFollowerState(RaftPersistentState(1, None, Vector()), RaftFollowerVolatileState(0, new DateTime(0)))

        router.processNodeMessage(1, request) // shouldBe (RaftFollower, state, List((0, IdleTimeoutMessage(0)), (1, AppendEntriesResponse(1, success = false))))
      }
    }
  }
}
