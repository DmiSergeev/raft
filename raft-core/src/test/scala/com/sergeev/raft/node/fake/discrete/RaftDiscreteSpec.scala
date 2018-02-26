package com.sergeev.raft.node.fake.discrete

import com.sergeev.raft.fake.discrete.RaftDiscreteSimulator
import org.scalatest.{Matchers, WordSpec}

class RaftDiscreteSpec extends WordSpec with Matchers {
  "simulator" should {
    "send proper acks" when {
      "===" in {
        val simulator = new RaftDiscreteSimulator()
        simulator.start()
        simulator.updateTime(10000)
      }
    }
  }
}
