package su.msk.dunno.scage.handlers.tracer

import su.msk.dunno.scage.support.Vec

trait Trace[S <: State] {
  def getCoord():Vec
  def getState():S
  def changeState(state:S):Unit
}