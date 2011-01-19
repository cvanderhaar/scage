package su.msk.dunno.blame.screens

import su.msk.dunno.screens.ScageScreen
import su.msk.dunno.scage.support.messages.ScageMessage
import su.msk.dunno.screens.support.ScageLibrary._
import org.lwjgl.input.Keyboard
import su.msk.dunno.scage.support.Vec
import su.msk.dunno.blame.field.FieldTracer
import su.msk.dunno.blame.field.tiles.{Door, Wall, Floor}
import su.msk.dunno.screens.prototypes.ScageRender
import su.msk.dunno.blame.prototypes.Decision
import su.msk.dunno.screens.handlers.Renderer
import su.msk.dunno.blame.support.{BottomMessages, TimeUpdater, GenLib}
import su.msk.dunno.blame.livings.{SiliconCreature, Cibo, Killy}
import su.msk.dunno.blame.decisions._

object Blamer extends ScageScreen(
  screen_name = "Blamer",
  is_main_screen = true,
  properties = "blame-properties.txt") {
  val right_messages_width = property("rightmessages.width", 200)
  
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
  val killy = FieldTracer.randomPassablePoint() match {
    case Some(point) => new Killy(point)
    case None => {
      log.error("failed to place killy to the field, the programm will exit")
      System.exit(1)
      null
    }
  }
  val cibo = FieldTracer.randomPassablePoint(killy.getPoint - Vec(2,2), killy.getPoint + Vec(2,2)) match {
    case Some(point) => new Cibo(point)
    case None => {
      log.error("failed to place cibo to the field, the programm will exit")
      System.exit(1)
      null
    }
  }
  def currentPlayer = if(is_play_cibo) cibo else killy
  
  // enemies
  (1 to 50).foreach(i => {
    FieldTracer.randomPassablePoint() match {
      case Some(point) => new SiliconCreature(point)
      case None =>
    }
  })

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
          TimeUpdater.addDecision(new Shoot(currentPlayer.selectTarget(Keyboard.KEY_F), currentPlayer)))
  keyListener(Keyboard.KEY_I, onKeyDown = currentPlayer.inventory.showInventory)
  keyListener(Keyboard.KEY_W, onKeyDown = currentPlayer.weapon.showWeapon)
  keyListener(Keyboard.KEY_D, onKeyDown = {
    TimeUpdater.addDecision(new DropItem(currentPlayer.inventory.selectItem(ScageMessage.xml("decision.drop.selection")), currentPlayer))
  })
  keyListener(Keyboard.KEY_COMMA, onKeyDown = {
    TimeUpdater.addDecision(new PickUpItem(currentPlayer))
  })
  
  keyListener(Keyboard.KEY_TAB, onKeyDown = is_play_cibo = !is_play_cibo)  
  keyListener(Keyboard.KEY_ESCAPE, onKeyDown = allStop)

  // render on main screen
  windowCenter = Vec((width - right_messages_width)/2, 
  		     BottomMessages.bottom_messages_height + (height - BottomMessages.bottom_messages_height)/2)
  center = FieldTracer.pointCenter(currentPlayer.getPoint)
  
  Renderer.backgroundColor = BLACK

  def drawInterface = {
    //messages on the right side of the screen
    ScageMessage.print(currentPlayer.stat("name"),          width - right_messages_width, height-25, WHITE)
    ScageMessage.print("FPS: "+Renderer.fps,                width - right_messages_width, height-45, WHITE)
    ScageMessage.print("time: "+TimeUpdater.time,           width - right_messages_width, height-65, WHITE)
    ScageMessage.print("HP: "+currentPlayer.intStat("health"), width - right_messages_width, height-85, WHITE)
  } 

  addRender(new ScageRender {
    override def render = FieldTracer.drawField(currentPlayer.getPoint)

    override def interface = {
      BottomMessages.showBottomMessages(0)
      drawInterface
    }
  })
  
  // initial message
  BottomMessages.addPropMessage("greetings.helloworld", currentPlayer.stat("name"))
  
  def main(args:Array[String]):Unit = run
}