package com.sergeev.raft.fake

import com.sergeev.raft.node.RaftCommand

case class RaftFakeCommand(info: String) extends RaftCommand
