package su.msk.dunno.blame.prototypes

import su.msk.dunno.screens.prototypes.ActionHandler
import su.msk.dunno.blame.screens.Blamer
import su.msk.dunno.scage.support.{Color, Vec}
import su.msk.dunno.blame.support.TimeUpdater

abstract class Npc(point:Vec, symbol:Int, color:Color) extends Living(point, symbol, color) {
  protected var last_decision:Decision = null
  Blamer.addHandler(new ActionHandler {
    override def action = {
      if(last_decision == null || TimeUpdater.time - lastActionTime >= last_decision.action_period) {
        last_decision = livingAI
        if(last_decision != null) TimeUpdater.addDecision(last_decision)
      }
    }
  })

  def livingAI:Decision
}