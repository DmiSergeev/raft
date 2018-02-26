package com.sergeev.raft.node

import com.sergeev.raft.node.environment.{RaftNetworkEndpoint, RaftScheduler, RaftStorage}
import com.sergeev.raft.node.message._
import com.sergeev.raft.node.role.{RaftFollower, RaftRole}
import com.sergeev.raft.node.state.{RaftState, RaftVolatileState}

trait RaftInstance {
  def processClientCommand(command: RaftCommand)

  def processNodeMessage(sender: NodeId, message: RaftMessage)
}

class RaftRouter(context: RaftContextImpl, network: RaftNetworkEndpoint, scheduler: RaftScheduler, storage: RaftStorage) extends RaftInstance {
  private var stateHolder: StateHolder[_ <: RaftState[_ <: RaftVolatileState[_], _]] =
    StateHolder(RaftFollower, RaftFollower.initializeState(storage.restore()))

  override def processClientCommand(command: RaftCommand): Unit = processMessage(None, ClientCommand(command))

  override def processNodeMessage(sender: NodeId, message: RaftMessage): Unit = processMessage(Some(sender), message)

  private def processMessage(sender: Option[NodeId], inMessage: RaftMessage): Unit = {
    val effectiveContext = context.copy(senderIdOption = sender)

    val processingResult = stateHolder.process(inMessage)(effectiveContext)
    val nextRole = processingResult._1
    val nextStateBeforeConvert = processingResult._2
    val outMessagesInfo = processingResult._3

    val oldState = stateHolder.state
    stateHolder = nextRole.convertState(nextStateBeforeConvert)

    val appliedCommands = stateHolder.state.appliedCommands(oldState)
    storage.save(stateHolder.state.persistent)

    for (command <- appliedCommands)
      network.sendClientAck(command)

    for ((target, outMessage) ← outMessagesInfo)
      outMessage match {
        case RetryProcessingMessage() ⇒ processMessage(sender, inMessage)
        case _: SelfImmediateMessage  ⇒ processMessage(Some(effectiveContext.selfId), outMessage)
        case _: SelfDeferredMessage ⇒ scheduler.schedule(outMessage.asInstanceOf[SelfDeferredMessage].interval, () ⇒ {
          processMessage(Some(effectiveContext.selfId), outMessage)
        })
        case _: ExternalTargetMessage ⇒ network.sendMessage(target, outMessage)
      }
  }
}

case class StateHolder[S <: RaftState[_ <: RaftVolatileState[_], S]](role: RaftRole[S], state: S) {
  def process(message: RaftMessage)(context: RaftContext): ProcessingResult[S] = role.processIncoming(message, state)(context)
}

