package scatris

import su.msk.dunno.scage.support.tracer.{StandardTracer, Trace, State}
import su.msk.dunno.scage.handlers.{Renderer}
import org.lwjgl.opengl.GL11
import su.msk.dunno.scage.support.{ScageLibrary, Vec}

class Point(init_coord:Vec, private val figure:Figure) extends ScageLibrary {
  val coord = init_coord
  private var is_active = true

  private val trace = StandardTracer.addTrace(new Trace[State] {
    override def isActive = is_active
    def getCoord = coord
    def getState() = new State("name", "point")
    def changeState(s:State) = if(s.contains("disable")) is_active = false
  })

  private var is_moving = true
  def isMoving = is_moving

  private val down = Vec(0, -StandardTracer.h_y)
  def moveDown = {
    if(!((coord in trace) --> (coord + down, -1 to 1, StandardTracer.h_y))) {
      is_moving = false
    }
  }
  def moveUp = (coord in trace) --> (coord - down, -1 to 1, StandardTracer.h_y)

  private val left = Vec(-StandardTracer.h_x, 0)
  def moveLeft = (coord in trace) --> (coord + left, -1 to 1, StandardTracer.h_x)

  private val right = Vec(StandardTracer.h_x, 0)
  def moveRight = (coord in trace) --> (coord + right, -1 to 1, StandardTracer.h_x)

  private val BOX = Renderer.createList("img/Crate.png", StandardTracer.h_x, StandardTracer.h_y, 0, 0, 256, 256)
  Renderer.addRender(() => {
    if(is_active) {
      GL11.glPushMatrix();
      GL11.glTranslatef(coord.x, coord.y, 0.0f);
      Renderer.setColor(WHITE)
      GL11.glCallList(BOX)
      GL11.glPopMatrix()
    }
  })
}