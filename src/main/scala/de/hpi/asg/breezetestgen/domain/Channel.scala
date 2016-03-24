package de.hpi.asg.breezetestgen.domain


object Channel {
  type Id = Int
  type Spec[+Channel] = Id

  sealed trait Endpoint
  case class CompEndpoint(id: HandshakeComponent.Id) extends Endpoint
  case class PortEndpoint(id: Port.Id) extends Endpoint

  implicit def brzCompId2CompEndpoint(id: HandshakeComponent.Id): CompEndpoint = CompEndpoint(id)
  implicit def portId2PortEndpoint(id: Port.Id): PortEndpoint = PortEndpoint(id)
}

sealed trait Channel[Comp] {
  def id: Channel.Id
  def active: Comp
  def passive: Comp
}

sealed trait NoPullChannel[Comp] extends Channel[Comp]
sealed trait NoPushChannel[Comp] extends Channel[Comp]

final case class SyncChannel[Comp](id: Channel.Id, active: Comp, passive: Comp)
  extends Channel[Comp] with NoPullChannel[Comp] with NoPushChannel[Comp]
final case class PullChannel[Comp](id: Channel.Id, active: Comp, passive: Comp)
  extends Channel[Comp] with NoPushChannel[Comp]
final case class PushChannel[Comp](id: Channel.Id, active: Comp, passive: Comp)
  extends Channel[Comp] with NoPullChannel[Comp]
