package su.msk.dunno.scar

import su.msk.dunno.scage.screens.physics.objects.StaticBox
import su.msk.dunno.scage.single.support.{ScageColor, Vec}
import su.msk.dunno.scage.screens.handlers.Renderer._
import net.phys2d.math.Vector2f
import org.lwjgl.opengl.GL11
import su.msk.dunno.scage.single.support.ScageColors._
import Scaranoid._

class TargetBox(leftup_coord:Vec) extends StaticBox(leftup_coord, 40, 40) {
  val box_color = {
    (math.random*3).toInt match {
      case 0 => RED
      case 1 => YELLOW
      case 2 => BLUE
    }
  }

  implicit def compareColors(color:ScageColor) = new ScalaObject {
    def >(other_color:ScageColor) = {
      color match {
        case RED => other_color == YELLOW
        case YELLOW => other_color == BLUE
        case BLUE => other_color == RED
        case _ => false
      }
    }
  }

  def colorModificator(box_color:ScageColor) = {
    if(PlayerBall.ball_color == WHITE) 4
    if(PlayerBall.ball_color > box_color) 3
    else if(PlayerBall.ball_color == box_color) 2
    else 1
  }

  action {
    if(isActive && isTouching(PlayerBall)) {
      Scaranoid.bonus = colorModificator(box_color)
      Scaranoid.count += Scaranoid.bonus

      PlayerBall.ball_color = box_color
      isActive = false

      if(Level.winCondition) pause()
    }
  }

  render {
    if(isActive) {
      color = box_color
      val verts:Array[Vector2f] = box.getPoints(body.getPosition(), body.getRotation());
      GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glBegin(GL11.GL_QUADS);
          verts.foreach(v => GL11.glVertex2f(v.getX, v.getY))
        GL11.glEnd();
      GL11.glEnable(GL11.GL_TEXTURE_2D);
    }
  }
}