package com.sergeev.raft.fake.discrete

import com.sergeev.raft.fake.RaftFakeCommand
import org.scalatest.{Matchers, WordSpec}

class RaftDiscreteSpec extends WordSpec with Matchers {
  "cluster" should {
    "choose leader" when {
      "no messages and no failures" in {
        val simulator = new RaftDiscreteSimulator()
        simulator.start()
        simulator.updateTime(2300)
        simulator.sendToLeader(RaftFakeCommand("cmd1"))
        simulator.updateTime(3000)
      }
    }
    "re-elect" when {
      "leader temporarily unavailable" in {
        val simulator = new RaftDiscreteSimulator()
        simulator.start()
        simulator.updateTime(2300)
        simulator.sendToLeader(RaftFakeCommand("cmd1"))

        simulator.updateTime(2400)
        assert(simulator.leader.nonEmpty)
        val initialLeader = simulator.leader.get
        simulator.settings.setUnavailableFrom(initialLeader)

        simulator.updateTime(4790)
        assert(simulator.leader.nonEmpty)
        assert(simulator.leader.get != initialLeader)
        simulator.settings.resetTimesFrom(initialLeader)

        simulator.updateTime(6000)
        simulator.restartInstance(5)

        simulator.updateTime(7000)
      }
    }
  }
}
