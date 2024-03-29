package su.msk.dunno.blame.screens

import su.msk.dunno.screens.ScageScreen
import org.lwjgl.input.Keyboard
import su.msk.dunno.scage.support.Vec
import su.msk.dunno.screens.handlers.Renderer
import su.msk.dunno.screens.prototypes.ScageRender
import su.msk.dunno.blame.support.BottomMessages._
import su.msk.dunno.screens.support.ScageLibrary._
import su.msk.dunno.blame.support.MyFont._
import su.msk.dunno.blame.prototypes.Living
import su.msk.dunno.scage.support.messages.ScageMessage._
import su.msk.dunno.blame.field.{FieldObject, FieldTracer}

class SelectTarget(val living:Living) extends ScageScreen("Target Selector") {
  private var _stop_key = -1
  private var target_point:Vec = living.getPoint
  private var select_line:List[Vec] = List(living.getPoint)
  def apply(new_key:Int):Vec = {
    _stop_key = new_key
    target_point = findTarget
    select_line = FieldTracer.line(living.getPoint, target_point)
    run
    target_point
  }

  private def findTarget:Vec = {
    val dov = living.getState.getInt("dov")
    FieldTracer.findVisibleObject(living.trace, living.getPoint, dov, obj => {
      obj.getState.contains("enemy") && obj.getState.getInt("health") > 0
    }) match {
      case Some(live_enemy) => live_enemy.getPoint
      case None => living.getPoint
    }
  }

  private def clearSelectLine = {
    select_line.foreach(FieldTracer.allowDraw(_))
    select_line = Nil
  }
  private def buildSelectLine(delta:Vec) = {
    target_point += delta
    clearSelectLine
    select_line = FieldTracer.line(living.getPoint, target_point)
    if(select_line.size > 1) select_line = select_line.tail
    select_line.foreach(FieldTracer.preventDraw(_))
    target_point = select_line.last
  }
  private def drawSelectLine = {
    if(!select_line.isEmpty) {
      select_line.init.foreach(point => {
        Renderer.drawDisplayList(MINOR_SELECTOR, FieldTracer.pointCenter(point), WHITE)
      })
      Renderer.drawDisplayList(MAIN_SELECTOR, FieldTracer.pointCenter(select_line.last), WHITE)
    }
  }  
  
  keyListener(Keyboard.KEY_NUMPAD9, 100, onKeyDown = buildSelectLine(Vec(1,1)))
  keyListener(Keyboard.KEY_NUMPAD8, 100, onKeyDown = buildSelectLine(Vec(0,1)))
  keyListener(Keyboard.KEY_NUMPAD7, 100, onKeyDown = buildSelectLine(Vec(-1,1)))
  keyListener(Keyboard.KEY_NUMPAD6, 100, onKeyDown = buildSelectLine(Vec(1,0)))
  keyListener(Keyboard.KEY_NUMPAD4, 100, onKeyDown = buildSelectLine(Vec(-1,0)))
  keyListener(Keyboard.KEY_NUMPAD3, 100, onKeyDown = buildSelectLine(Vec(1,-1)))
  keyListener(Keyboard.KEY_NUMPAD2, 100, onKeyDown = buildSelectLine(Vec(0,-1)))
  keyListener(Keyboard.KEY_NUMPAD1, 100, onKeyDown = buildSelectLine(Vec(-1,-1)))

  keyListener(Keyboard.KEY_UP,    100, onKeyDown = buildSelectLine(Vec(0,1)))
  keyListener(Keyboard.KEY_RIGHT, 100, onKeyDown = buildSelectLine(Vec(1,0)))
  keyListener(Keyboard.KEY_LEFT,  100, onKeyDown = buildSelectLine(Vec(-1,0)))
  keyListener(Keyboard.KEY_DOWN,  100, onKeyDown = buildSelectLine(Vec(0,-1)))

  keyListener(_stop_key, onKeyDown = {
    clearSelectLine
    stop
  })

  keyListener(Keyboard.KEY_ESCAPE, onKeyDown = {
    target_point = living.getPoint
    clearSelectLine
    stop
  })

  // render on main screen
  windowCenter = Vec((width - Blamer.right_messages_width)/2,
  		            bottom_messages_height + (height - bottom_messages_height)/2)
  center = FieldTracer.pointCenter(living.getPoint)
  
  Renderer.backgroundColor = BLACK  
  
  addRender(new ScageRender {
    override def render = {
      FieldTracer.drawField(Blamer.currentPlayer.getPoint)
      drawSelectLine
    }
    
    override def interface {
      FieldTracer.objectsAtPoint(target_point) match {
        case head :: tail => print(xml("selecttarget.target")+" "+head.getState.getString("name"),
                                10, bottom_messages_height - row_height)
        case _ =>
      }
      print(xml("selecttarget.helpmessage"),
        10, bottom_messages_height - (row_height*2), GREEN)
      showBottomMessages(2)
      Blamer.drawInterface
    }
  })
}
