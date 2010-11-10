package su.msk.dunno.blame.screens

import su.msk.dunno.screens.Screen
import su.msk.dunno.blame.support.MyFont._
import su.msk.dunno.scage.support.messages.Message
import su.msk.dunno.screens.support.ScageLibrary._
import org.lwjgl.input.Keyboard
import su.msk.dunno.scage.support.Vec
import su.msk.dunno.screens.handlers.Renderer
import su.msk.dunno.blame.field.tiles.{Wall, Floor}
import su.msk.dunno.blame.field.FieldTracer
import su.msk.dunno.screens.prototypes.Renderable
import su.msk.dunno.blame.support.{MyFont, GenLib}
import su.msk.dunno.blame.livings.Killy

object FieldScreen extends Screen("Field Screen") {
  override def properties = "blame-properties.txt"

  val game_from_x = property("game_from_x", 0)
  val game_to_x = property("game_to_x", 800)
  val game_from_y = property("game_from_y", 0)
  val game_to_y = property("game_to_y", 600)
  val N_x = property("N_x", 16)
  val N_y = property("N_y", 12)
  val fieldTracer = new FieldTracer(game_from_x, game_to_x, game_from_y, game_to_y, N_x, N_y, true)

  val maze = GenLib.CreateMaze(N_x, N_y)
  (0 to N_x-1).foreachpair(0 to N_y-1)((i, j) => {
    if(maze(i)(j) == '#') new Floor(i, j, fieldTracer)
    else if(maze(i)(j) == '.') new Wall(i, j, fieldTracer)
  })

  val killy = new Killy(fieldTracer.getRandomPassablePoint, fieldTracer)
  keyListener(Keyboard.KEY_UP, onKeyDown = killy.move(Vec(0,1)))
  keyListener(Keyboard.KEY_DOWN, onKeyDown = killy.move(Vec(0,-1)))
  keyListener(Keyboard.KEY_RIGHT, onKeyDown = killy.move(Vec(1,0)))
  keyListener(Keyboard.KEY_LEFT, onKeyDown = killy.move(Vec(-1,0)))
  
  Renderer.background(BLACK)

  addRender(new Renderable {
    override def render {
	    fieldTracer.drawField
    }

    override def interface {
      Message.print("Message Message Message Message Message ", 10, 80, WHITE)
      Message.print("Message Message Message Message Message ", 10, 60, WHITE)
      Message.print("Message Message Message Message Message ", 10, 40, WHITE)
      Message.print("Message Message Message Message Message ", 10, 20, WHITE)
      Message.print("Message Message Message Message Message ", 10, 0, WHITE)

      Message.print("FPS: "+fps, 600, height-25, WHITE)
    }
  })
  
  keyListener(Keyboard.KEY_ESCAPE, onKeyDown = allStop)
  
  def main(args:Array[String]):Unit = run
}
