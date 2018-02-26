package com.sergeev.raft.node.state

import com.sergeev.raft.node.{RaftCommand, RaftEntry}
import org.joda.time.DateTime
import org.scalatest.{Matchers, WordSpec}

class RaftStateSpec extends WordSpec with Matchers {
  val command1: RaftCommand = new RaftCommand {}
  val command2: RaftCommand = new RaftCommand {}
  val command3: RaftCommand = new RaftCommand {}
  val command4: RaftCommand = new RaftCommand {}
  val command5: RaftCommand = new RaftCommand {}

  val state = RaftFollowerState(RaftPersistentState(1, None, Vector(RaftEntry(1, command1), RaftEntry(1, command2), RaftEntry(1, command3))),
    RaftFollowerVolatileState(0, new DateTime(0)))

  "patchLog" should {
    "change nothing" when {
      "no entries" in {
        val nextState = state.patchLog(2, Vector(), 2)
        nextState.persistent.log shouldBe Vector(RaftEntry(1, command1), RaftEntry(1, command2), RaftEntry(1, command3))
      }
      "new entries already in log" in {
        val nextState = state.patchLog(0, Vector(RaftEntry(1, command2), RaftEntry(1, command3)), 2)
        nextState.persistent.log shouldBe Vector(RaftEntry(1, command1), RaftEntry(1, command2), RaftEntry(1, command3))
      }
    }

    "just append new entries" when {
      "previous log ends exactly before new entries" in {
        val nextState = state.patchLog(2, Vector(RaftEntry(1, command4), RaftEntry(1, command5)), 4)
        nextState.persistent.log shouldBe Vector(RaftEntry(1, command1), RaftEntry(1, command2), RaftEntry(1, command3), RaftEntry(1, command4), RaftEntry(1, command5))
      }
    }

    "rewrite entries and clean all existing after mismatch" when {
      "there is a mismatch between existing and new entries" in {
        val nextState = state.patchLog(0, Vector(RaftEntry(2, command4)), 4)
        nextState.persistent.log shouldBe Vector(RaftEntry(1, command1), RaftEntry(2, command4))
      }
    }
  }
}
