package de.hpi.asg.breezetestgen.actors

import akka.actor.Actor
import de.hpi.asg.breezetestgen.Loggable
import de.hpi.asg.breezetestgen.domain.Netlist
import de.hpi.asg.breezetestgen.domain.components.HandshakeComponent
import de.hpi.asg.breezetestgen.testgeneration.TestOp

object TestGenerationActor {
  case object Start
}

class TestGenerationActor(protected val netlist: Netlist) extends Actor with MainNetlistCreator with Loggable {
  import TestGenerationActor._

  def receive = {
    case Start =>
      //TODO: create InfoHub
      //TODO: create netlistActor
      //TODO: send Request on activate to netlistActor
    case testOp: TestOp =>
      //TODO: let infoHub handle this
      //TODO reply with new TestEvent
    case r: HandshakeComponent.Reaction =>
      //TODO: merge this with testop above?!
      //TODO: forward to infoHub - stop search?!
    //case DecisionRequired =>
      //TODO: create some constraintCollections to test feasibility
      //TODO: filter unfeasible possibilities
      //TODO: (decide between remaining)
      //TODO: save state of/duplicate InformationHub and NetlistActor
      //TODO: transfer new state to netlist and send according signals
  }
}