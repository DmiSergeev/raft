package com.sergeev.raft.util

import scala.util.Random

object implicits {
  implicit class RangeRandom(val range: Range) {
    def rand(random: Random): Int = range.start + random.nextInt(range.length)
  }
}

