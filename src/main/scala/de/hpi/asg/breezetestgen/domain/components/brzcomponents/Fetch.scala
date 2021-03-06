package de.hpi.asg.breezetestgen.domain.components.brzcomponents

import de.hpi.asg.breezetestgen.domain.components.BrzComponent._
import de.hpi.asg.breezetestgen.domain.components.{BrzComponent, BrzComponentBehaviour, HandshakeComponent}

class Fetch(id: HandshakeComponent.Id,
            activate: SyncSpec,
            inp: PullSpec,
            out: PushSpec) extends BrzComponent(id) {
  type Behaviour = FetchBehaviour
  type C = FetchBehaviour.ControlState
  type D = Null

  def behaviour(state: Option[HandshakeComponent.State[C, D]]): Behaviour =
    new FetchBehaviour(state getOrElse FetchBehaviour.freshState)


  object FetchBehaviour {
    sealed trait ControlState
    case object Idle extends ControlState
    case object WaitForInp extends ControlState
    case object WaitForOut extends ControlState

    val freshState: HandshakeComponent.State[ControlState, Null] = HandshakeComponent.State(Idle, null)
  }

  class FetchBehaviour(initState: HandshakeComponent.State[C, D]) extends BrzComponentBehaviour[C, D](initState) {
    import FetchBehaviour._

    info(s"$id: Created FetchBehaviour with state: $initState")

    when(Idle) {
      case Req(`activate`, _) =>
        info(s"$id: Activated")
        request(inp)
        goto(WaitForInp)
    }

    when(WaitForInp) {
      case DataAck(`inp`, data, _) =>
        info(s"$id: read $data")
        dataRequest(out, data)  // no new constraint, because data is just passed through
        goto(WaitForOut)
    }

    when(WaitForOut) {
      case Ack(`out`, _) =>
        info(s"$id: wrote")
        acknowledge(activate)
        goto(Idle)
    }

    initialize()
  }
}
