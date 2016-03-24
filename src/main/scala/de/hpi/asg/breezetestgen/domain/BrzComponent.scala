package de.hpi.asg.breezetestgen.domain

import de.hpi.asg.breezetestgen.Loggable

/** base class for Breeze components
  *
  * A Breeze handshake component with its wiring is represented by subclasses of this.
  * During a session, only one instance of this will be created per component in the netlist.
  * However, the components behaviour could be instantiated using multiple different states.
  *
  */
abstract class BrzComponent(val id: HandshakeComponent.Id) extends HandshakeComponent with Loggable {
  type Behaviour <: ComponentBehaviour[_, _]
  type C
  type D

  def behaviour(option: Option[ComponentState[C, D]]): Behaviour
}
