package su.msk.dunno.scage.screens.support.physics.objects

import su.msk.dunno.scage.single.support.Vec
import su.msk.dunno.scage.screens.support.physics.Physical
import net.phys2d.math.{ROVector2f, Vector2f}
import net.phys2d.raw.shapes.Polygon
import net.phys2d.raw.StaticBody

class StaticPolygon(val vertices:Vec*) extends Physical {
  val polygon_vertices = for {
    vertice <- vertices
    new_vertice = vertice - vertices(0)
  } yield new Vector2f(new_vertice.x, new_vertice.y)
  val polygon = new Polygon(polygon_vertices.toArray)
  val body = new StaticBody("StaticPolygon", polygon)
  body.setPosition(vertices(0).x, vertices(0).y)

  private val vertices_array = vertices.toArray
  def points = vertices_array
}