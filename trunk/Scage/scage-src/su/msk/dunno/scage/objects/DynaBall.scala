package su.msk.dunno.scage.objects

import net.phys2d.raw.Body
import net.phys2d.raw.shapes.Circle
import su.msk.dunno.scage.prototypes.Physical
import su.msk.dunno.scage.handlers.{Physics, Renderer}
import su.msk.dunno.scage.support.{Colors, Vec}

class DynaBall(init_coord:Vec, radius:Int, val enableRender:Boolean = true) extends Physical with Colors {
  val body = new Body(new Circle(radius), 2);
  body.setPosition(init_coord.x, init_coord.y)
  Physics.addBody(body)

  Renderer.addRender(() => render())
  protected def render() = {
    Renderer.setColor(BLACK)
    Renderer.drawCircle(coord, radius)
//`    Message.print(Tracer.point(coord), coord)
    Renderer.drawLine(coord, coord+velocity)
  }
}