package de.hpi.asg.breezetestgen.domain

import de.hpi.asg.breezetestgen.testgeneration.TestOp
import de.hpi.asg.breezetestgen.testing.TestEvent
import de.hpi.asg.breezetestgen.util.FSM

object ComponentBehaviour {
  type ConstraintsNVariables = Int // TODO replace this with correct one

  case class Reaction(signals: Set[Signal], testOp: Option[TestOp], cvs: Set[ConstraintsNVariables]) {
    def addSignal(s: Signal): Reaction = copy(signals = signals + s)
    def setTestOp(op: TestOp):Reaction = copy(testOp = Option(op))
    def addConstraint(cv: ConstraintsNVariables): Reaction = copy(cvs = cvs + cv)
    def addConstraints(new_cvs: Traversable[ConstraintsNVariables]): Reaction = copy(cvs = cvs ++ new_cvs)
  }
  object Reaction {
    def empty[DT <: Data]: Reaction = Reaction(Set.empty, None, Set.empty)
  }
}

/** base class for the behaviour definiton of BrzComponents
  *
  * @param initState the state of the component to start from
  * @tparam C control state
  * @tparam D data state
  */
abstract class ComponentBehaviour[C, D] protected(initState: ComponentState[C, D])
  extends FSM[C, D, (Signal, TestEvent)] {
  import ComponentBehaviour._
  startWith(initState.controlState, initState.dataState)

  // reaction will be build up with helper methods during signal handling
  private[this] var reaction: Reaction = _

  /** holds the testEvent of current signal, if needed */
  protected var testEvent: TestEvent = _

  /** processes one step of fsm with given input */
  def handleSignal(s: Signal, te: TestEvent): Reaction = {
    reaction = Reaction.empty
    testEvent = te
    processMsg((s, te))
    reaction
  }

  /** returns complete current state of the FSM, which can be used for replicating the FSM */
  def state = ComponentState(myState.stateName, myState.stateData)

  private def addSignal(s: Signal) = reaction = reaction.addSignal(s)

  /** helper methods for signaling other components */
  protected def request(channelId: Channel.Spec[NoPushChannel[_]]) = addSignal(Request(channelId))
  protected def acknowledge(channelId: Channel.Spec[NoPullChannel[_]]) = addSignal(Acknowledge(channelId))
  protected def dataRequest(channelId: Channel.Spec[PushChannel[_]], data: Data) = addSignal(DataRequest(channelId, data))
  protected def dataAcknowledge(channelId: Channel.Spec[PullChannel[_]], data: Data) = addSignal(DataAcknowledge(channelId, data))

  /** helper method for constraint addition */
  protected def constrain(cv: ConstraintsNVariables*) = reaction = reaction.addConstraints(cv)
  /** helper method for setting testOp*/
  protected def testOp(op: TestOp) = reaction = reaction.setTestOp(op)


  // Message Event Extractors
  object Req {
    def unapply(e: Event): Option[(Channel.Spec[NoPushChannel[_]], D, TestEvent)] = {
      e match {
        case FSM.Event((Request(c), te), i) => Some(c, i, te)
        case _ => None
      }
    }
  }
  object Ack {
    def unapply(e: Event): Option[(Channel.Spec[NoPullChannel[_]], D, TestEvent)] = {
      e match {
        case FSM.Event((Acknowledge(c), te), i) => Some(c, i, te)
        case _ => None
      }
    }
  }
  object DataReq {
    def unapply(e: Event): Option[(Channel.Spec[PushChannel[_]], Data, D, TestEvent)] = {
      e match {
        case FSM.Event((DataRequest(c, d), te), i) => Some(c, d, i, te)
        case _ => None
      }
    }
  }
  object DataAck {
    def unapply(e: Event): Option[(Channel.Spec[PullChannel[_]], Data, D, TestEvent)] = {
      e match {
        case FSM.Event((DataAcknowledge(c, d), te), i) => Some(c, d, i, te)
        case _ => None
      }
    }
  }

  type UnhandledException = FSM.UnhandledException[Signal, D]
}
