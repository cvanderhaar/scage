package su.msk.dunno.blame.field

import su.msk.dunno.screens.support.tracer.{Tracer, Trace}
import su.msk.dunno.screens.handlers.Renderer
import rlforj.los.{ILosBoard, PrecisePermissive}
import su.msk.dunno.scage.support.{Colors, Vec, Color}
import su.msk.dunno.blame.support.MyFont
import su.msk.dunno.scage.support.ScageProperties._

trait FieldObject extends Trace {
  def getSymbol:Int
  def getColor:Color
  def isTransparent:Boolean
  def isPassable:Boolean
  
  private var was_drawed = false
  def draw = {
    Renderer.drawDisplayList(getSymbol, getCoord, getColor)
    was_drawed = true
  }
  def drawGray = Renderer.drawDisplayList(getSymbol, getCoord, Colors.GRAY)
  def wasDrawed = was_drawed
}

object FieldTracer extends Tracer[FieldObject](
  property("game_from_x", 0), 
  property("game_to_x", 800), 
  property("game_from_y", 0), 
  property("game_to_y", 600), 
  property("N_x", 16), 
  property("N_y", 12), 
  true) {
  def isPointOnArea(x:Int, y:Int) = {
    x >= 0 && x < N_x && y >= 0 && y < N_y
  }

  def isPointPassable(x:Int, y:Int):Boolean = {
    isPointOnArea(x, y) && (matrix(x)(y).length == 0 || matrix(x)(y).forall(_.isPassable))
  }
  def isPointPassable(point:Vec):Boolean = isPointPassable(point.ix, point.iy)
  
  def isPointTransparent(x:Int, y:Int) = {
    isPointOnArea(x, y) && (matrix(x)(y).length == 0 || matrix(x)(y).forall(_.isTransparent))
  }
  
  def isLocationPassable(coord:Vec) = {
    val p = point(coord)
    isPointPassable(p.ix, p.iy)
  }

  def getRandomPassablePoint:Vec = {
    log.debug("looking for new random passable point")

    var x = -1
    var y = -1

    var count = 10
    while(!isPointPassable(x, y) && count > 0) {
      x = (math.random*N_x).toInt
      y = (math.random*N_y).toInt

      count -= 1
    }
    if(count == 0 && !isPointPassable(x, y))
      log.warn("warning: cannot locate random passable point within "+count+" tries")

    Vec(x, y)
  }
  
  def move2PointIfPassable(trace_id:Int, old_point:Vec, new_point:Vec) = {
    if(isPointPassable(new_point)) {
      val old_coord = pointCenter(old_point)
      val new_coord = pointCenter(new_point)
      updateLocation(trace_id, old_coord, new_coord)
      old_point is new_point
      true
    }
    else false
  }
  
  def neighboursOfPoint(trace_id:Int, point:Vec, range:Range) = {
    neighbours(trace_id, pointCenter(point), -1 to 1, (f) => true)
  }

  private var lightSources:List[() => Vec] = Nil
  def addLightSource(coord: => Vec) = lightSources = (() => coord) :: lightSources
  
  private val pp = new PrecisePermissive();  
  private val drawView = new ILosBoard() {
    def contains(x:Int, y:Int):Boolean = isPointOnArea(x, y)    
    def isObstacle(x:Int, y:Int):Boolean = !isPointTransparent(x, y);
    def visit(x:Int, y:Int) = {
      if(matrix(x)(y).length > 0) matrix(x)(y).head.draw
    }   
  }

  def draw(player_point:Vec) = {
    drawGray(player_point)
    drawEnlighted(player_point)
  }
  private val distance_from_player = math.min(N_x/2, N_y/2)*math.min(N_x/2, N_y/2)
  private def drawEnlighted(player_point:Vec) = {
    lightSources.filter(source => (source() dist2 player_point) < distance_from_player).foreach(source => {
      pp.visitFieldOfView(drawView, source().ix, source().iy, 5)  
    })
  }

  val visible_width  = property("visible_width",  Renderer.width - 200)
  val visible_height = property("visible_height", Renderer.height - 100)

  private val half_visible_N_x:Int = visible_width/h_x/2
  private val half_visible_N_y:Int = visible_height/h_y/2
  private def drawGray(player_point:Vec) = {
    val from_x = math.max(0,     player_point.ix - half_visible_N_x)
    val to_x   = math.min(N_x-1, player_point.ix + half_visible_N_x)
    val from_y = math.max(0,     player_point.iy - half_visible_N_y)
    val to_y   = math.min(N_y-1, player_point.iy + half_visible_N_y)
    for(x <- from_x to to_x) {
      for(y <- from_y to to_y) {
        if(matrix(x)(y).length > 0) {
          val tile = matrix(x)(y).head
          if(tile.wasDrawed && tile.getSymbol != MyFont.FLOOR) matrix(x)(y).head.drawGray
        }
      }
    }
  }
}
