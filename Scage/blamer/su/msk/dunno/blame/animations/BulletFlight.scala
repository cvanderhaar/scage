package su.msk.dunno.blame.animations

import su.msk.dunno.screens.ScageScreen
import su.msk.dunno.blame.support.IngameMessages
import su.msk.dunno.blame.screens.FieldScreen
import su.msk.dunno.scage.support.{Color, Vec}
import su.msk.dunno.screens.prototypes.{ActionHandler, Renderable}
import su.msk.dunno.blame.field.{FieldObject, FieldTracer}
import su.msk.dunno.screens.support.tracer.State
import su.msk.dunno.blame.support.MyFont._
import su.msk.dunno.screens.support.ScageLibrary._
import su.msk.dunno.screens.handlers.Renderer

class BulletFlight(val start_point:Vec, val end_point:Vec, val color:Color, val delay:Long = 30)
extends ScageScreen("Bullet Flight") {
  private val line = FieldTracer.line(end_point, start_point).toArray
  private var count = 0

  val trace = FieldTracer.addTrace(new FieldObject {
    def getCoord = FieldTracer.pointCenter(line(count))
    def getSymbol = BULLET
    def getColor = color
    def isTransparent = true
    def isPassable = true

    def getState = new State
    def changeState(s:State) = {}
  })

  FieldTracer.addLightSource(line(count))

  private var last_move_time = System.currentTimeMillis
  addHandler(new ActionHandler {
    override def action = {
      if(System.currentTimeMillis - last_move_time > delay) {
        if(count < line.length-1) {
          FieldTracer.updatePointLocation(trace, line(count), line(count+1))
          count += 1
          last_move_time = System.currentTimeMillis
        }
        else stop
      }
    }

    override def exit = {
      FieldTracer.removeTraceFromPoint(trace, line(count))
    }
  })

  // render on main screen
  windowCenter = Vec((width - 200)/2, 100 + (height - 100)/2)
  center = FieldTracer.pointCenter(FieldScreen.currentPlayer.point)

  Renderer.backgroundColor(BLACK)  

  addRender(new Renderable {
    override def render = FieldTracer.draw(FieldScreen.currentPlayer.point)

    override def interface {
      IngameMessages.showBottomMessages
      FieldScreen.drawInterface
    }
  })

  run
}