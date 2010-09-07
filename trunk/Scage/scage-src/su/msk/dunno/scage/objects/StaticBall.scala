package su.msk.dunno.scage.objects

import su.msk.dunno.scage.prototypes.Physical
import net.phys2d.raw.shapes.Circle
import net.phys2d.raw.{StaticBody}
import su.msk.dunno.scage.handlers.{Physics, Renderer}
import su.msk.dunno.scage.support.{Colors, Vec}

class StaticBall(init_coord:Vec, radius:Int) extends Physical with Colors {
  def this(init_coord:Vec) = this(init_coord, 1)

  val body = new StaticBody("circle", new Circle(radius));
  body.setPosition(init_coord.x, init_coord.y)
  Physics.addBody(body)

  Renderer.addRender(() => {
    Renderer.setColor(BLACK)
    Renderer.drawCircle(coord, radius)
    Renderer.drawLine(coord, coord+velocity)
  })
}