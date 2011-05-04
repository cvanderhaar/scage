package su.msk.dunno.scage.screens.physics

import net.phys2d.raw.World
import su.msk.dunno.scage.single.support.ScageProperties._
import net.phys2d.math.Vector2f
import net.phys2d.raw.strategies.QuadSpaceStrategy

class Physics {
  val dt = property("dt", 5)
  val world = new World(new Vector2f(0.0f, 0), 10, new QuadSpaceStrategy(20,10));
  world.enableRestingBodyDetection(0.01f, 0.000001f, 0.01f)
  
  private var physicals:List[Physical] = Nil
  def --> (physical:Physical) = {
    if(!world.getBodies.contains(physical.body)) world.add(physical.body)
    if(!physicals.contains(physical)) physicals = physical :: physicals
    physical.prepare()

    physical
  }

  def step() {
    for(p <- physicals) {
      if(!p.isActive) {
        world.remove(p.body)
        physicals = physicals.filterNot(_ == p)
      }
      else p.isTouching = false
    }

    for(i <- 1 to dt) {
      world.step()
      for(p <- physicals) {
        p.isTouching = p.isTouching || p.body.getTouching.size > 0
      }
    }
  }
}