package com.sergeev.raft.node

case class RaftEntry(term: RaftTerm, command: RaftCommand)
