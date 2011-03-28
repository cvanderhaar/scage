package su.msk.dunno.scage.screens.physics

import su.msk.dunno.scage.screens.ScageScreen
import _root_.net.phys2d.raw.{Body, World}
import su.msk.dunno.scage.single.support.Vec
import su.msk.dunno.scage.single.support.ScageProperties._
import net.phys2d.math.Vector2f
import _root_.net.phys2d.raw.strategies.QuadSpaceStrategy

class PhysicsScreen(screen_name:String, is_main_screen:Boolean = false, properties:String = "")
extends ScageScreen(screen_name, is_main_screen, properties) {
  val dt = property("dt", 5)
  val world = new World(new Vector2f(0.0f, 0), 10, new QuadSpaceStrategy(20,10));
  world.enableRestingBodyDetection(0.01f, 0.000001f, 0.01f)
  
  private var physicals:List[Physical] = Nil
  def addBody(physical:Physical) {
    if(!world.getBodies.contains(physical.body)) {
      init {
        world.add(physical.body)
        physicals = physical :: physicals
      }

      render {
        if(physical.isActive) physical.render
      }

      exit {
        physicals.foreach(p => world.remove(p.body))
        physicals = Nil
      }
    }
  }
  def removeBody(physical:Physical) {
    world.remove(physical.body)
    physicals = physicals.filterNot(_ == physical)
  }

  action {
    if(!onPause) {
      physicals.foreach(_.isTouching = false)
      for(i <- 1 to dt) {
        world.step()
        physicals.foreach(p => {
          if(!p.isActive) removeBody(p)
          else p.isTouching = p.isTouching || p.body.getTouching.size > 0
        })
      }
    }
  }
}