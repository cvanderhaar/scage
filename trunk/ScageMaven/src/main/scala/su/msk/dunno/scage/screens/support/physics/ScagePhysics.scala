package su.msk.dunno.scage.screens.support.physics

import net.phys2d.raw.World
import su.msk.dunno.scage.single.support.ScageProperties._
import net.phys2d.math.Vector2f
import net.phys2d.raw.strategies.QuadSpaceStrategy
import su.msk.dunno.scage.single.support.Vec

class ScagePhysics {
  val dt = property("physics.dt", 5)
  val gravity = Vec(property("physics.gravity.x", 0.0f), property("physics.gravity.y", 0.0f))
  val world = new World(new Vector2f(gravity.x, gravity.y), 10, new QuadSpaceStrategy(20,10));
  world.enableRestingBodyDetection(0.01f, 0.000001f, 0.01f)
  
  private var physicals:List[Physical] = Nil
  def addPhysical(physical:Physical) = {
    if(!world.getBodies.contains(physical.body)) world.add(physical.body)
    if(!physicals.contains(physical)) physicals = physical :: physicals
    physical.prepare()

    physical
  }

  def removeAll() {
    physicals.foreach(p => world.remove(p.body))
    physicals = Nil
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