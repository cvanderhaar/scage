package su.msk.dunno.blame.screens

import su.msk.dunno.screens.ScageScreen
import su.msk.dunno.scage.support.messages.Message
import su.msk.dunno.screens.support.ScageLibrary._
import org.lwjgl.input.Keyboard
import su.msk.dunno.scage.support.Vec
import su.msk.dunno.blame.field.FieldTracer
import su.msk.dunno.blame.field.tiles.{Door, Wall, Floor}
import su.msk.dunno.screens.prototypes.Renderable
import su.msk.dunno.blame.prototypes.Decision
import su.msk.dunno.screens.handlers.Renderer
import su.msk.dunno.blame.support.{IngameMessages, TimeUpdater, GenLib}
import su.msk.dunno.blame.livings.{SiliconCreature, Cibo, Killy}
import su.msk.dunno.blame.decisions.{Shoot, CloseDoor, OpenDoor, Move}

object Blamer extends ScageScreen("Blamer", is_main_screen = true, "blame-properties.txt") {
  // map
  private val maze = GenLib.CreateStandardDunegon(FieldTracer.N_x, FieldTracer.N_y)
  (0 to FieldTracer.N_x-1).foreachpair(0 to FieldTracer.N_y-1)((i, j) => {
    maze(i)(j) match {
      case '#' => new Wall(i, j)
      case '.' => new Floor(i, j)
      case ',' => new Floor(i, j)
      case '+' => new Door(i, j)
      case _ =>
    }
  })
  
  // players
  private var is_play_cibo = false
  val killy = new Killy(FieldTracer.randomPassablePoint())
  val cibo = new Cibo(FieldTracer.randomPassablePoint(killy.point - Vec(2,2), killy.point + Vec(2,2)))
  def currentPlayer = if(is_play_cibo) cibo else killy
  
  // enemies
  (1 to 50).foreach(i => new SiliconCreature(FieldTracer.randomPassablePoint()))

  // controls on main screen
  private var is_key_pressed = false
  private var pressed_start_time:Long = 0
  private def repeatTime = {
    if(is_key_pressed) {
      if(System.currentTimeMillis - pressed_start_time > 600) 100
      else 300
    }
    else 300
  }
  private def press(decision:Decision) = {
    if(!is_key_pressed) {
      is_key_pressed = true
      pressed_start_time = System.currentTimeMillis
    }
    TimeUpdater.addDecision(decision)
  }
  
  keyListener(Keyboard.KEY_NUMPAD9, repeatTime, 
    onKeyDown = press(new Move(Vec(1,1), currentPlayer)), onKeyUp = is_key_pressed = false)
  keyListener(Keyboard.KEY_UP,      repeatTime, 
    onKeyDown = press(new Move(Vec(0,1), currentPlayer)), onKeyUp = is_key_pressed = false)
  keyListener(Keyboard.KEY_NUMPAD8, repeatTime, 
    onKeyDown = press(new Move(Vec(0,1), currentPlayer)),   onKeyUp = is_key_pressed = false)
  keyListener(Keyboard.KEY_NUMPAD7, repeatTime, 
    onKeyDown = press(new Move(Vec(-1,1), currentPlayer)),  onKeyUp = is_key_pressed = false)
  keyListener(Keyboard.KEY_RIGHT,   repeatTime, 
    onKeyDown = press(new Move(Vec(1,0), currentPlayer)),   onKeyUp = is_key_pressed = false)
  keyListener(Keyboard.KEY_NUMPAD6, repeatTime, 
    onKeyDown = press(new Move(Vec(1,0), currentPlayer)),   onKeyUp = is_key_pressed = false)
  keyListener(Keyboard.KEY_NUMPAD5, repeatTime,
    onKeyDown = press(new Move(Vec(0,0), currentPlayer)),   onKeyUp = is_key_pressed = false)
  keyListener(Keyboard.KEY_LEFT,    repeatTime, 
    onKeyDown = press(new Move(Vec(-1,0), currentPlayer)),  onKeyUp = is_key_pressed = false)
  keyListener(Keyboard.KEY_NUMPAD4, repeatTime, 
    onKeyDown = press(new Move(Vec(-1,0), currentPlayer)),  onKeyUp = is_key_pressed = false)
  keyListener(Keyboard.KEY_NUMPAD3, repeatTime, 
    onKeyDown = press(new Move(Vec(1,-1), currentPlayer)),  onKeyUp = is_key_pressed = false)
  keyListener(Keyboard.KEY_DOWN,    repeatTime, 
    onKeyDown = press(new Move(Vec(0,-1), currentPlayer)),  onKeyUp = is_key_pressed = false)
  keyListener(Keyboard.KEY_NUMPAD2, repeatTime, 
    onKeyDown = press(new Move(Vec(0,-1), currentPlayer)),  onKeyUp = is_key_pressed = false)
  keyListener(Keyboard.KEY_NUMPAD1, repeatTime, 
    onKeyDown = press(new Move(Vec(-1,-1), currentPlayer)), onKeyUp = is_key_pressed = false)
  
  keyListener(Keyboard.KEY_O, onKeyDown = TimeUpdater.addDecision(new OpenDoor(currentPlayer)))
  keyListener(Keyboard.KEY_C, onKeyDown = TimeUpdater.addDecision(new CloseDoor(currentPlayer)))
  keyListener(Keyboard.KEY_F, onKeyDown =
          TimeUpdater.addDecision(new Shoot(SelectTarget.targetPoint(Keyboard.KEY_F, currentPlayer), currentPlayer)))
  keyListener(Keyboard.KEY_I, onKeyDown = InventoryScreen.show(currentPlayer.inventory))
  
  keyListener(Keyboard.KEY_TAB, onKeyDown = is_play_cibo = !is_play_cibo)  
  keyListener(Keyboard.KEY_ESCAPE, onKeyDown = allStop)

  // render on main screen
  windowCenter = Vec((width - 200)/2, 100 + (height - 100)/2)
  center = FieldTracer.pointCenter(currentPlayer.point)
  
  Renderer.backgroundColor(BLACK)
  
  def drawInterface = {
    Message.print(currentPlayer.stat("name"), 600, height-25, WHITE)
    Message.print("FPS: "+fps, 600, height-45, WHITE)
    Message.print("time: "+TimeUpdater.time, width - 200, height-65, WHITE)
    Message.print("HP: "+currentPlayer.stat("health"), width - 200, height-85, WHITE)
  } 
  
  addRender(new Renderable {
    override def render = FieldTracer.draw(currentPlayer.point)

    override def interface {
      IngameMessages.showBottomMessages
      drawInterface
    }
  })
  
  // initial message
  IngameMessages.addBottomPropMessage("greetings.helloworld", currentPlayer.stat("name"))
  
  def main(args:Array[String]):Unit = run
}